package zula.app;


import zula.client.ConnectionManager;
import zula.common.commands.Command;
import zula.common.commands.DragonByIdCommand;
import zula.common.commands.GetListOfCommands;
import zula.common.data.Color;
import zula.common.data.Coordinates;
import zula.common.data.Dragon;
import zula.common.data.DragonCave;
import zula.common.data.DragonType;
import zula.common.data.ResponseCode;
import zula.common.data.ServerMessage;
import zula.common.exceptions.GetServerMessageException;
import zula.common.exceptions.PrintException;
import zula.common.exceptions.SendException;
import zula.common.exceptions.WrongArgumentException;
import zula.common.exceptions.WrongCommandException;
import zula.common.util.ArgumentParser;
import zula.common.util.IoManager;
import zula.util.ArgumentReader;
import zula.util.CommandParser;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;


public class AppForExecuteScript {
    private static final Logger APPLOGGER = Logger.getLogger("App logger");
    private final IoManager ioManager;
    private final ConnectionManager connectionManager;
    private HashMap<String, Command> commands;
    private final ArgumentParser argumentParser = new ArgumentParser();
    private String login;
    private String password;
    private ResourceBundle currentBundle;
    public AppForExecuteScript(IoManager ioManager, ConnectionManager connectionManager, ResourceBundle currentBundle) {
        this.ioManager = ioManager;
        this.currentBundle = currentBundle;
        this.connectionManager = connectionManager;
    }

    public void startApp() throws PrintException {


        try {
            connectionManager.sendToServer(new GetListOfCommands(),  new Serializable[]{""});
            ServerMessage serverMessage = connectionManager.getMessage();
            commands = (HashMap<String, Command>) serverMessage.getArguments()[0];
        } catch (SendException | GetServerMessageException | ClassCastException e) {
            ioManager.getOutputManager().write(currentBundle.getString("Impossible to get commands"));
            return;
        }
        while (ioManager.isProcessStillWorks()) {
            readAndExecute();
        }
    }
   /* private void authenticate() throws PrintException, WrongArgumentException {
        try {
            ioManager.getOutputManager().write("Если вы новый пользователь, зарегистрируйтесь, введя команду register, иначе введите login");
            String command = ioManager.getInputManager().read(ioManager);
            if ("login".equals(command)) {
                LoginCommand loginCommand = new LoginCommand();
                Serializable[] args = readArgsForLoginAndRegistration();
                connectionManager.sendToServer(loginCommand, args);
            } else if ("register".equals(command)) {
                RegisterCommand registerCommand = new RegisterCommand();
                Serializable[] args = readArgsForLoginAndRegistration();
                connectionManager.sendToServer(registerCommand, args);
            } else {
                ioManager.getOutputManager().write("Неверная команда");
                throw new WrongArgumentException();
            }
            connectionManager.setLogin(login);
            connectionManager.setPassword(password);
            ServerMessage serverMessage = connectionManager.getMessage();
            if (serverMessage.getResponseStatus() == ResponseCode.OK) {
                ioManager.getOutputManager().write(serverMessage.getArguments()[0]);
            } else {
                ioManager.getOutputManager().write(serverMessage.getArguments()[0]);
                throw new WrongArgumentException();
            }
        } catch (SendException | GetServerMessageException e) {
            e.printStackTrace();
        }
    }*/
/*    private Serializable[] readArgsForLoginAndRegistration() throws PrintException {
        ioManager.getOutputManager().write("Введите логин");
        login = ioManager.getInputManager().read(ioManager);
        ioManager.getOutputManager().write("Введите пароль");
        password = ioManager.getInputManager().read(ioManager);
        return new Serializable[]{login, password};
    }*/


    private void readAndExecute() throws PrintException {
        try {
            ioManager.getOutputManager().write(currentBundle.getString("Enter command!"));
            String readLine = ioManager.getInputManager().read(ioManager);
            if (readLine == null) {
                ioManager.exitProcess();
                return;
            }
            String command = parseCommand(readLine);
            Serializable args;
            try {
                args = readArgs(command, readLine);
            } catch (WrongArgumentException e) {
                ioManager.getOutputManager().write(currentBundle.getString(e.getMessage()));
                return;
            }
            if ("exit".equals(command)) {
                ioManager.exitProcess();
            }
            connectionManager.sendToServer(commands.get(command), new Serializable[]{args});
            ServerMessage serverMessage = connectionManager.getMessage();
            String answer = serverMessage.getArguments()[0].toString();
            ioManager.getOutputManager().write(answer);
        } catch (NoSuchElementException e) {
            ioManager.exitProcess();
        } catch (WrongCommandException e) {
            ioManager.getOutputManager().write(currentBundle.getString("There is no such command"));
        } catch (SendException e) {
            ioManager.getOutputManager().write(currentBundle.getString("Error sending response"));
            ioManager.exitProcess();
        } catch (GetServerMessageException e) {
            ioManager.getOutputManager().write(currentBundle.getString("Error while getting response"));
            ioManager.exitProcess();
        }
    }
    private String parseCommand(String readLine)  throws WrongCommandException {
        String command;
        command = CommandParser.commandParse(readLine, ioManager);
        if (!commands.containsKey(command)) {
            throw new WrongCommandException();
        }
        APPLOGGER.info("Из входных данных получена команда");
        return command;
    }
    private String parseArgs(String command, String readLine) {
        String newLine = (readLine.replace(command, ""));
        if (newLine.length() >= 1 && newLine.charAt(0) == ' ') {
            newLine = newLine.substring(1);
        }
        return newLine;
    }

    private Serializable readArgs(String command, String argument) throws WrongArgumentException, PrintException, GetServerMessageException, SendException {
        String commandArguments = parseArgs(command, argument);
        Serializable arguments = commandArguments;
        if ("add".equals(command)) {
          arguments = parseAdd(commandArguments);
        }
        if ("remove_by_id".equals(command) || "remove_lower".equals(command)) {
            arguments = argumentParser.parseArgFromString(commandArguments, Objects::nonNull, Integer::parseInt);
        }
        if ("update_id".equals(command)) {
            arguments = parseUpdate(commandArguments);
        }
        if ("execute_script".equals(command)) {
            if (ioManager.getInputManager().containsFileInStack(commandArguments)) {
                throw new WrongArgumentException("The Threat of Recursion!");
            }
            try {
                ioManager.getInputManager().setFileReading(true, commandArguments);
            } catch (FileNotFoundException e) {
                throw new WrongArgumentException("File not found or no permissions");
            }
        }
        if ("add".equals(command) || "execute_script".equals(command) || "remove_by_id".equals(command) || "remove_lower".equals(command) || "update_id".equals(command)) {
            APPLOGGER.info("Получены аргументы");
            return arguments;
        } else {
            if ("".equals(commandArguments)) {
                APPLOGGER.info("Получены аргументы");
                return "";
            }
            throw new WrongArgumentException("Wrong args");

        }
    }

    private Serializable parseAdd(String commandArguments) throws WrongArgumentException {
        if (!argumentParser.checkIfTheArgsEmpty(commandArguments)) {
            throw new WrongArgumentException("Wrong args");
        }
        try {
            return readAddArgs();
        } catch (PrintException e) {
            ioManager.exitProcess();
            throw new WrongArgumentException("");
        }
    }

    private Serializable parseUpdate(String commandArguments) throws WrongArgumentException, PrintException, SendException, GetServerMessageException {
        if (argumentParser.checkIfTheArgsEmpty(commandArguments)) {
            throw new WrongArgumentException("Wrong args");
        }
        ServerMessage serverMessage;
        connectionManager.sendToServer(new DragonByIdCommand(), new Serializable[]{Integer.parseInt(commandArguments)});
        serverMessage = connectionManager.getMessage();

        if (serverMessage.getResponseStatus().equals(ResponseCode.OK)) {
            Dragon dragon = (Dragon) readAddArgs();
            dragon.setId(Integer.parseInt(commandArguments));
            return dragon;
            } else {
                throw new WrongArgumentException(serverMessage.getArguments()[0].toString());
            }
    }
    private Serializable readAddArgs() throws PrintException {
        ArgumentReader argumentReader = new ArgumentReader(ioManager);
            String name = argumentReader.readName();
            Coordinates coordinates = argumentReader.readCoordinates();
            long age = argumentReader.readAge();
            float wingspan = argumentReader.readWingspan();
            Color color = argumentReader.readColor();
            DragonType type = argumentReader.readType();
            DragonCave cave = argumentReader.readCave();
            return new Dragon(name, coordinates, age, wingspan, color, type, cave);
    }




}
