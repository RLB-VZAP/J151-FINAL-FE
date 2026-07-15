package za.ac.vzap.trytons.frontend.client;

public final class TransferRequestValidator {
    private TransferRequestValidator() {}

    public static boolean isValid(TransferRequest request){
        return request != null
                && !isBlank(request.getTeamId())
                && !isBlank(request.getRoundId())
                && !isBlank(request.getRemovedPlayerId())
                && !isBlank(request.getAddedPlayerId())
                && !request.getRemovedPlayerId().equals(request.getAddedPlayerId());
    }

    private static boolean isBlank(String value){
        return value == null || value.isBlank();
    }
}
