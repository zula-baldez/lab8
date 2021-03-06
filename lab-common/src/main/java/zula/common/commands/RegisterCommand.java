package zula.common.commands;

import zula.common.data.ResponseCode;
import zula.common.data.ServerMessage;
import zula.common.exceptions.PrintException;
import zula.common.util.AbstractClient;
import zula.common.util.IoManager;

import java.io.Serializable;

public class RegisterCommand extends Command {
    @Override
    public ServerMessage doInstructions(IoManager ioManager, AbstractClient client, Serializable[] arguments) throws PrintException {
        String login = arguments[0].toString();
        String password = arguments[1].toString();
        if (client.getSqlManager().register(login, password, client) == ResponseCode.OK) {
            return new ServerMessage("Successful registration", ResponseCode.OK);
        } else {
            return new ServerMessage("Error registering, please try another username", ResponseCode.ERROR);

        }


    }
    @Override
    public boolean isNeedsLoginCheck() {
        return false;
    }
}
