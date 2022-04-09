package zula.common.commands;


import zula.common.exceptions.PrintException;
import zula.common.util.CollectionManager;
import zula.common.util.IoManager;

import java.io.Serializable;

public class Clear extends Command {
    @Override
    public void doInstructions(IoManager ioManager, CollectionManager collectionManager, Serializable argument) throws PrintException {
        collectionManager.clearDragons();
        ioManager.getOutputManager().write("Команда выполнена!");
    }
}
