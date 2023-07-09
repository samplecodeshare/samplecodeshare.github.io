import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;

public class JsonToProtobufConverter {

    public static void main(String[] args) {
        // JSON data
        String jsonData = "{\"field1\":\"value1\", \"field2\":\"value2\"}";

        try {
            // Convert JSON to Protobuf
            YourProtobufMessage.Builder protobufBuilder = YourProtobufMessage.newBuilder();
            JsonFormat.parser().merge(jsonData, protobufBuilder);

            // Get Protobuf message
            YourProtobufMessage protobufMessage = protobufBuilder.build();

            // Convert Protobuf to byte array
            byte[] protobufBytes = protobufMessage.toByteArray();

            // Print the Protobuf message
            System.out.println("Protobuf message:\n" + protobufMessage.toString());

            // Parse the Protobuf message from bytes
            YourProtobufMessage parsedProtobufMessage = YourProtobufMessage.parseFrom(protobufBytes);

            // Print the parsed Protobuf message
            System.out.println("Parsed Protobuf message:\n" + parsedProtobufMessage.toString());
        } catch (InvalidProtocolBufferException | IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
