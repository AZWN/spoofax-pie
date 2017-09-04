package mb.pipe.run.spoofax.util;

import java.util.ArrayList;

import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.messages.MessageType;

import com.google.inject.Inject;

import mb.pipe.run.core.PipeRunEx;
import mb.pipe.run.core.model.message.Msg;
import mb.pipe.run.core.model.message.MsgConstants;
import mb.pipe.run.core.model.message.MsgImpl;
import mb.pipe.run.core.model.message.MsgSeverity;
import mb.pipe.run.core.model.message.MsgType;
import mb.pipe.run.core.model.message.PathMsg;
import mb.pipe.run.core.model.message.PathMsgImpl;
import mb.pipe.run.core.model.region.Region;
import mb.pipe.run.core.parse.ParseMsgType;
import mb.vfs.path.PPath;

public class MessageConverter {
    private final PathConverter pathConverter;


    @Inject public MessageConverter(PathConverter pathConverter) {
        this.pathConverter = pathConverter;
    }


    public Msg toMsg(IMessage message) {
        if(message.source() != null) {
            return toPathMsg(message);
        }
        final String text = message.message();
        final MsgSeverity severity = toSeverity(message.severity());
        final MsgType type = toType(message.type());
        final Region region = message.region() != null ? RegionConverter.toRegion(message.region()) : null;
        final Throwable exception = message.exception();
        final MsgImpl msg = new MsgImpl(text, severity, type, region, exception);
        return msg;
    }

    public ArrayList<Msg> toMsgs(Iterable<IMessage> messages) {
        final ArrayList<Msg> msgs = new ArrayList<Msg>();
        for(IMessage message : messages) {
            final Msg msg = toMsg(message);
            msgs.add(msg);
        }
        return msgs;
    }

    public PathMsg toPathMsg(IMessage message) {
        if(message.source() == null) {
            throw new PipeRunEx("Cannot convert Spoofax Core message to a path message, source has not been set");
        }
        final String text = message.message();
        final MsgSeverity severity = toSeverity(message.severity());
        final MsgType type = toType(message.type());
        final Region region = message.region() != null ? RegionConverter.toRegion(message.region()) : null;
        final Throwable exception = message.exception();
        final PPath path = pathConverter.toPath(message.source());
        final PathMsgImpl msg = new PathMsgImpl(text, severity, type, region, exception, path);
        return msg;
    }

    public ArrayList<PathMsg> toPathMsgs(Iterable<IMessage> messages) {
        final ArrayList<PathMsg> msgs = new ArrayList<PathMsg>();
        for(IMessage message : messages) {
            final PathMsg msg = toPathMsg(message);
            msgs.add(msg);
        }
        return msgs;
    }


    private static MsgSeverity toSeverity(MessageSeverity messageSeverity) {
        switch(messageSeverity) {
            case ERROR:
                return MsgConstants.errorSeverity;
            case WARNING:
                return MsgConstants.warningSeverity;
            case NOTE:
                return MsgConstants.infoSeverity;
            default:
                throw new PipeRunEx("Cannot convert Spoofax Core message severity " + messageSeverity
                    + " to a pipeline message severity");
        }
    }

    private static MsgType toType(MessageType messageType) {
        switch(messageType) {
            case PARSER:
                return new ParseMsgType();
            case ANALYSIS:
            case TRANSFORMATION:
            case INTERNAL:
                return MsgConstants.internalType;
            default:
                throw new PipeRunEx(
                    "Cannot convert Spoofax Core message type " + messageType + " to a pipeline message type");
        }
    }
}
