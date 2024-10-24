package org.example;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class Main {

    public static void main(String[] args) {
        // Создание основной платформы JADE
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        AgentContainer mainContainer = rt.createMainContainer(p);

        try {
            // Создание и запуск агентов
            AgentController agent1 = mainContainer.createNewAgent("agent1", "org.example.FunctionAgent", new Object[]{"agent2", "agent3"});
            AgentController agent2 = mainContainer.createNewAgent("agent2", "org.example.FunctionAgent", new Object[]{"agent1", "agent3"});
            AgentController agent3 = mainContainer.createNewAgent("agent3", "org.example.FunctionAgent", new Object[]{"agent1", "agent2"});

            agent1.start();
            agent2.start();
            agent3.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}