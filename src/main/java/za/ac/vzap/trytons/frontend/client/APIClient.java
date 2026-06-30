package za.ac.vzap.trytons.frontend.client;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.ObjectMapper;
import za.ac.vzap.trytons.frontend.util.APIConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class APIClient {
    private HttpClient client;
    private ObjectMapper mapper;
    private String baseUrl;
    public static class ApiResult<T>{
        @Getter
        @Setter
        private boolean success;
        @Getter
        @Setter
        private T data;
        private String message;
        private String errorCode;

        private ApiResult(boolean success, T data, String message, String errorCode) {
            this.success = success;
            this.data = data;
            this.message = message;
            this.errorCode = errorCode;
        }
        public static <T> ApiResult<T> success(T data) {
            return new ApiResult<>(true,data,null,null);
        }
        public static <T> ApiResult<T> fail(String message, String errorCode) {
            return new ApiResult<>(false,null,message,errorCode);
        }

    }
    private static class ErrorBody {
        public boolean success;
        public String message;
        public String errorCode;
    }

    public APIClient(){
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.mapper = new ObjectMapper();
        this.baseUrl = APIConfig.getBaseUrl();
    }
    public <T> ApiResult<T> post(String path, Object requestBody, Class<T> responseType){
        try{
            String json = requestBody == null ? "" : mapper.writeValueAsString(requestBody);
            String Json = "";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", Json)
                    .header("Accept",Json)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return send(request, responseType);
        }catch(Exception e){
            return ApiResult.fail("Unable to reach server. Please try again.","REQUEST_ERROR");
        }
    }
    public <T> ApiResult <T> get(String path, Class<T> responseType){
        try{
            String Json = "";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Accept",Json)
                    .GET()
                    .build();
            return send(request, responseType);
        }catch(Exception e){
            return ApiResult.fail("Unable to reach server.Please try again.","CLIENT_ERROR");
        }
    }

    private <T> ApiResult <T> send (HttpRequest request, Class<T> responseType){
        try{
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            int status  = response.statusCode();
            String body = response.body();
            if(status>=200 && status<300){
                T parsed = null;
                if(responseType != Void.class){
                    parsed = mapper.readValue(body, responseType);
                }
                return ApiResult.success(parsed);
            }
            try{
                ErrorBody errorBody = mapper.readValue(body, ErrorBody.class);
                return ApiResult.fail(errorBody.message, errorBody.errorCode);
            }catch(Exception e){
                return ApiResult.fail("Request failed (" + status + ").","UNKNOWN_ERROR");
            }
        }catch(Exception e){
            return ApiResult.fail("Unable to reach the server. Please try again.","CLIENT_ERROR");
        }
    }


}
