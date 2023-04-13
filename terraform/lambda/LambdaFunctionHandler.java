import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private static final String TABLE_NAME = "unisync-table";
    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            // get the data to be written to DynamoDB from the event
            String dataJson = event.getBody();
            Data data = objectMapper.readValue(dataJson, Data.class);

            // write the data to DynamoDB
            Table table = dynamoDB.getTable(TABLE_NAME);
            Item item = new Item()
                    .withPrimaryKey("id", data.getId());
            table.putItem(item);

            // return a response indicating success or failure
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody("Data written to DynamoDB");
        } catch (JsonProcessingException e) {
            // return an error response
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error writing data to DynamoDB");
        }
    }
    
    private static class Data {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
