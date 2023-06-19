package tests;

import java.util.Set;

import package_server.Server;



public class TestManager {

    private static TestManager instance;
    private Server server;


    public static TestManager getInstance(){
        if(instance == null)
            instance = new TestManager();
        return instance;
    }

    private TestManager(){
        server = Server.getInstance();
        server.testModeLogin();
        FileResetter.setFileContents();
    }

    public Server getServer(){
        return server;
    }

    public static int getMax(Set<Integer> set){
        int max = 0;
        for(int i: set)
            max = Math.max(i, max);
        return max;
    }
    
}