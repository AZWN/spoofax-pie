package mb.common.message;

import java.io.Serializable;
import java.util.ArrayList;

public class Messages implements Serializable {
    final ArrayList<Message> messages;


    public Messages(ArrayList<Message> messages) {
        this.messages = messages;
    }


    public int size() {
        return messages.size();
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void accept(MessageVisitor visitor) {
        for(Message msg : messages) {
            if(msg.region == null) {
                if(!visitor.noOrigin(msg.text, msg.exception, msg.severity)) return;
            } else {
                if(!visitor.regionOrigin(msg.text, msg.exception, msg.severity, msg.region)) return;
            }
        }
    }

    public void accept(GeneralMessageVisitor visitor) {
        for(Message msg : messages) {
            if(!visitor.message(msg.text, msg.exception, msg.severity, null, msg.region)) return;
        }
    }


    public boolean containsSeverity(Severity severity) {
        return messages.stream().anyMatch(
            message -> message.severity.equals(severity)
        );
    }

    public boolean containsError() {
        return containsSeverity(Severity.Error);
    }

    public boolean containsWarning() {
        return containsSeverity(Severity.Warning);
    }

    public boolean containsInfo() {
        return containsSeverity(Severity.Info);
    }

    public boolean containsDebug() {
        return containsSeverity(Severity.Debug);
    }

    public boolean containsTrace() {
        return containsSeverity(Severity.Trace);
    }


    public boolean containsSeverityOrHigher(Severity severity) {
        return messages.stream().anyMatch(
            message -> message.severity.compareTo(severity) >= 0
        );
    }

    public boolean containsErrorOrHigher() {
        return containsSeverityOrHigher(Severity.Error);
    }

    public boolean containsWarningOrHigher() {
        return containsSeverityOrHigher(Severity.Warning);
    }

    public boolean containsInfoOrHigher() {
        return containsSeverityOrHigher(Severity.Info);
    }

    public boolean containsDebugOrHigher() {
        return containsSeverityOrHigher(Severity.Debug);
    }

    public boolean containsTraceOrHigher() {
        return containsSeverityOrHigher(Severity.Trace);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Messages that = (Messages) o;
        return messages.equals(that.messages);
    }

    @Override public int hashCode() {
        return messages.hashCode();
    }

    @Override public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        messages.forEach((m) -> {
            stringBuilder.append(m.toString());
            stringBuilder.append('\n');
        });
        return stringBuilder.toString();
    }
}
