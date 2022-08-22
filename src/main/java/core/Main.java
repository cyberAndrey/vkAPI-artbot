package core;

import core.utils.DataBase;

public class Main {

    public static void main(String[] args) throws Exception {
        VKCore vkCore;
        vkCore = new VKCore();
        vkCore.start();
        DataBase.closeConnection();

    }
}
