package mb.pipe.run.spoofax.sdf;

import static org.spoofax.jsglr.client.imploder.AbstractTokenizer.*;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getTokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.MultiBadTokenException;
import org.spoofax.jsglr.client.ParseTimeoutException;
import org.spoofax.jsglr.client.RegionRecovery;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.Token;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.TokenExpectedException;

import mb.pipe.run.core.model.message.Msg;
import mb.pipe.run.core.model.message.MsgBuilder;
import mb.pipe.run.core.model.region.Region;
import mb.pipe.run.core.parse.ParseMsgType;

public class ParserErrorHandler {
    private static final int LARGE_REGION_SIZE = 8;
    private static final String LARGE_REGION_START =
        "Region could not be parsed because of subsequent syntax error(s) indicated below";

    private final boolean recoveryEnabled;
    private final boolean recoveryFailed;
    private final Set<BadTokenException> parseErrors;

    private final ArrayList<Msg> messages = new ArrayList<>();
    private final MsgBuilder msgBuilder = new MsgBuilder().withType(new ParseMsgType()).withoutException();



    public ParserErrorHandler(boolean recoveryEnabled, boolean recoveryFailed, Set<BadTokenException> parseErrors) {
        this.recoveryEnabled = recoveryEnabled;
        this.recoveryFailed = recoveryFailed;
        this.parseErrors = parseErrors;
    }


    public ArrayList<Msg> messages() {
        return messages;
    }


    /*
     * Non-fatal (recoverable) errors
     */

    public void gatherNonFatalErrors(IStrategoTerm top) {
        final ITokenizer tokenizer = getTokenizer(top);
        if(tokenizer != null) {
            for(int i = 0, max = tokenizer.getTokenCount(); i < max; i++) {
                final IToken token = tokenizer.getTokenAt(i);
                final String error = token.getError();
                if(error != null) {
                    if(error == ITokenizer.ERROR_SKIPPED_REGION) {
                        i = findRightMostWithSameError(token, null);
                        reportSkippedRegion(token, tokenizer.getTokenAt(i));
                    } else if(error.startsWith(ITokenizer.ERROR_WARNING_PREFIX)) {
                        i = findRightMostWithSameError(token, null);
                        reportWarningAtTokens(token, tokenizer.getTokenAt(i), error);
                    } else if(error.startsWith(ITokenizer.ERROR_WATER_PREFIX)) {
                        i = findRightMostWithSameError(token, ITokenizer.ERROR_WATER_PREFIX);
                        reportErrorAtTokens(token, tokenizer.getTokenAt(i), error);
                    } else {
                        i = findRightMostWithSameError(token, null);
                        // UNDONE: won't work for multi-token errors (as seen in
                        // SugarJ)
                        reportErrorAtTokens(token, tokenizer.getTokenAt(i), error);
                    }
                }
            }
        }
    }

    private static int findRightMostWithSameError(IToken token, String prefix) {
        final String expectedError = token.getError();
        final ITokenizer tokenizer = token.getTokenizer();
        int i = token.getIndex();
        for(int max = tokenizer.getTokenCount(); i + 1 < max; i++) {
            String error = tokenizer.getTokenAt(i + 1).getError();
            if(error != expectedError && (error == null || prefix == null || !error.startsWith(prefix)))
                break;
        }
        return i;
    }

    private void reportSkippedRegion(IToken left, IToken right) {
        // Find a parse failure(s) in the given token range
        int line = left.getLine();
        int reportedLine = -1;
        for(BadTokenException e : getCollectedErrorsInRegion(left, right, true)) {
            processFatalException(left.getTokenizer(), e);
            if(reportedLine == -1)
                reportedLine = e.getLineNumber();
        }

        if(reportedLine == -1) {
            // Report entire region
            reportErrorAtTokens(left, right, ITokenizer.ERROR_SKIPPED_REGION);
        } else if(reportedLine - line >= LARGE_REGION_SIZE) {
            // Warn at start of region
            reportErrorAtTokens(findLeftMostTokenOnSameLine(left), findRightMostTokenOnSameLine(left),
                LARGE_REGION_START);
        }
    }

    private List<BadTokenException> getCollectedErrorsInRegion(IToken left, IToken right, boolean alsoOutside) {
        final List<BadTokenException> results = new ArrayList<>();
        final int line = left.getLine();
        final int endLine = right.getLine() + (alsoOutside ? RegionRecovery.NR_OF_LINES_TILL_SUCCESS : 0);
        for(BadTokenException error : parseErrors) {
            if(error.getLineNumber() >= line && error.getLineNumber() <= endLine) {
                results.add(error);
            }
        }
        return results;
    }


    /*
     * Fatal errors
     */

    public void processFatalException(ITokenizer tokenizer, Exception exception) {
        try {
            throw exception;
        } catch(ParseTimeoutException e) {
            reportTimeOut(tokenizer, e);
        } catch(TokenExpectedException e) {
            reportTokenExpected(tokenizer, e);
        } catch(MultiBadTokenException e) {
            reportMultiBadToken(tokenizer, e);
        } catch(BadTokenException e) {
            reportBadToken(tokenizer, e);
        } catch(Exception e) {
            createErrorAtFirstLine("Internal parsing error: " + e);
        }
    }

    private void reportTimeOut(ITokenizer tokenizer, ParseTimeoutException exception) {
        final String message = "Internal parsing error: " + exception.getMessage();
        createErrorAtFirstLine(message);
        reportMultiBadToken(tokenizer, exception);
    }

    private void reportTokenExpected(ITokenizer tokenizer, TokenExpectedException exception) {
        final String message = exception.getShortMessage();
        reportErrorNearOffset(tokenizer, exception.getOffset(), message);
    }

    private void reportMultiBadToken(ITokenizer tokenizer, MultiBadTokenException exception) {
        for(BadTokenException e : exception.getCauses()) {
            processFatalException(tokenizer, e);
        }
    }

    private void reportBadToken(ITokenizer tokenizer, BadTokenException exception) {
        final String message;
        if(exception.isEOFToken() || tokenizer.getTokenCount() <= 1) {
            message = exception.getShortMessage();
        } else {
            IToken token = tokenizer.getTokenAtOffset(exception.getOffset());
            token = findNextNonEmptyToken(token);
            message = ITokenizer.ERROR_WATER_PREFIX + ": " + token.toString().trim();
        }
        reportErrorNearOffset(tokenizer, exception.getOffset(), message);
    }


    private void reportErrorNearOffset(ITokenizer tokenizer, int offset, String message) {
        final IToken errorToken = tokenizer.getErrorTokenOrAdjunct(offset);
        final Region region = RegionFactory.fromTokens(errorToken, errorToken);
        reportErrorAtRegion(region, message);
    }

    private static IToken findNextNonEmptyToken(IToken token) {
        final ITokenizer tokenizer = token.getTokenizer();
        IToken result = null;
        for(int i = token.getIndex(), max = tokenizer.getTokenCount(); i < max; i++) {
            result = tokenizer.getTokenAt(i);
            if(result.getLength() != 0 && !Token.isWhiteSpace(result))
                break;
        }
        return result;
    }


    private void createErrorAtFirstLine(String text) {
        final String errorText = text + getErrorExplanation();
        final Msg msg = msgBuilder.asError().withoutRegion().withText(errorText).build();
        messages.add(msg);
    }

    private void reportErrorAtTokens(IToken left, IToken right, String text) {
        reportErrorAtRegion(RegionFactory.fromTokens(left, right), text);
    }

    private void reportErrorAtRegion(Region region, String text) {
        final Msg msg = msgBuilder.asError().withRegion(region).withText(text).build();
        messages.add(msg);
    }

    private void reportWarningAtTokens(IToken left, IToken right, String text) {
        reportWarningAtRegion(RegionFactory.fromTokens(left, right), text);
    }

    private void reportWarningAtRegion(Region region, String text) {
        final Msg msg = msgBuilder.asWarning().withRegion(region).withText(text).build();
        messages.add(msg);
    }


    private String getErrorExplanation() {
        if(recoveryFailed) {
            return " (recovery failed)";
        } else if(!recoveryEnabled) {
            return " (recovery disabled)";
        } else {
            return "";
        }
    }
}
