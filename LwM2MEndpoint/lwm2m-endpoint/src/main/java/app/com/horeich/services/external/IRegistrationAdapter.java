package app.com.horeich.services.external;

import java.util.concurrent.*;

import app.com.horeich.services.exceptions.*;

public interface IRegistrationAdapter
{
    CompletionStage<Void> RegisterDeviceAsync(String serialNumber) throws BaseException;
}