package org.example;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.*;
import java.util.Random;

public class FunctionAgent extends Agent {

    private double x; // Текущее значение аргумента
    private double delta; // Текущее значение сдвига
    private boolean flg; // Флаг, указывающий, является ли агент инициатором
    private String[] knownAgents; // Массив известных агентов
    private boolean finished; // Флаг, указывающий, что агент завершил свою работу

    @Override
    protected void setup() {
        // Инициализация начальных значений
        x = new Random().nextDouble() * 10; // Случайная начальная точка в диапазоне [0, 10]
        delta = 3.0; // Начальное значение delta
        flg = false; // По умолчанию агент не является инициатором
        finished = false; // Агент не завершил свою работу

        // Получение известных агентов из аргументов запуска
        Object[] args = getArguments(); // Получаем аргументы, переданные при создании агента
        if (args != null && args.length > 0) {
            knownAgents = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                knownAgents[i] = (String) args[i]; // Сохраняем имена известных агентов
            }
        }

        // Проверка, является ли этот агент инициатором
        if (getLocalName().equals("agent1")) {
            flg = true; // Если имя агента "agent1", то он становится инициатором
            addBehaviour(new InitiatorBehaviour()); // Добавляем поведение инициатора
        } else {
            addBehaviour(new ReceiveRequestBehaviour()); // Добавляем поведение для приема запросов
        }
    }

    // Метод для отображения всплывающего окна с результатом
    private void showResultPopup(double x, double y) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "Final result: x = " + x + ", y = " + y,
                    "Result",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // Поведение инициатора
    private class InitiatorBehaviour extends Behaviour {
        @Override
        public void action() {
            if (finished) {
                return; // Если агент уже завершил свою работу, ничего не делаем
            }

            // Отправка запроса на расчет значений функций
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST); // Создаем сообщение с запросом
            for (String agentName : knownAgents) {
                request.addReceiver(new jade.core.AID(agentName, jade.core.AID.ISLOCALNAME)); // Добавляем всех известных агентов в список получателей
            }
            request.setContent(x + "," + delta); // Устанавливаем содержимое сообщения (текущее значение x и delta)
            send(request); // Отправляем сообщение

            // Расчет значений своей функции
            double y1 = FunctionHelper.calculateFunction1(x - delta); // Расчет значения функции для x - delta
            double y2 = FunctionHelper.calculateFunction1(x); // Расчет значения функции для x
            double y3 = FunctionHelper.calculateFunction1(x + delta); // Расчет значения функции для x + delta

            // Ожидание ответов от других агентов
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM); // Создаем шаблон для сообщений с ответами
            ACLMessage reply = blockingReceive(mt); // Блокируем выполнение до получения ответа
            while (reply != null) {
                // Обработка ответа
                String[] parts = reply.getContent().split(","); // Разбиваем содержимое ответа на части
                double y1Other = Double.parseDouble(parts[0]); // Получаем значение функции для x - delta от другого агента
                double y2Other = Double.parseDouble(parts[1]); // Получаем значение функции для x от другого агента
                double y3Other = Double.parseDouble(parts[2]); // Получаем значение функции для x + delta от другого агента

                // Определение экстремума суммарного значения функций
                double sum1 = y1 + y1Other; // Сумма значений функций для x - delta
                double sum2 = y2 + y2Other; // Сумма значений функций для x
                double sum3 = y3 + y3Other; // Сумма значений функций для x + delta

                if (sum1 > sum2 && sum1 > sum3) {
                    x -= delta; // Если сумма для x - delta максимальна, обновляем x
                } else if (sum3 > sum2 && sum3 > sum1) {
                    x += delta; // Если сумма для x + delta максимальна, обновляем x
                } else {
                    delta /= 2; // Иначе уменьшаем delta в два раза
                }

                // Проверка условия завершения
                if (delta < 2) {
                    double finalY = FunctionHelper.calculateFunction1(x);
                    showResultPopup(x, finalY); // Показываем всплывающее окно с результатом
                    finished = true; // Устанавливаем флаг завершения работы
                    doDelete(); // Удаляем агента
                    return; // Прекращаем выполнение поведения
                }

                // Передача очереди следующему агенту
                String nextAgentName = knownAgents[new Random().nextInt(knownAgents.length)]; // Выбираем случайного следующего агента
                ACLMessage passQueue = new ACLMessage(ACLMessage.INFORM); // Создаем сообщение для передачи очереди
                passQueue.addReceiver(new jade.core.AID(nextAgentName, jade.core.AID.ISLOCALNAME)); // Добавляем следующего агента в список получателей
                passQueue.setContent(x + "," + delta); // Устанавливаем содержимое сообщения (текущее значение x и delta)
                send(passQueue); // Отправляем сообщение

                reply = blockingReceive(mt); // Ожидаем следующий ответ
            }
        }

        @Override
        public boolean done() {
            return finished; // Поведение завершается, когда агент завершил свою работу
        }
    }

    // Поведение для приема запросов на расчет значений функций
    private class ReceiveRequestBehaviour extends Behaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST); // Создаем шаблон для сообщений с запросами
            ACLMessage request = receive(mt); // Получаем запрос
            if (request != null) {
                String[] parts = request.getContent().split(","); // Разбиваем содержимое запроса на части
                double x = Double.parseDouble(parts[0]); // Получаем значение x из запроса
                double delta = Double.parseDouble(parts[1]); // Получаем значение delta из запроса

                // Расчет значений функции
                double y1, y2, y3;
                if (getLocalName().equals("agent2")) {
                    y1 = FunctionHelper.calculateFunction2(x - delta); // Расчет значения функции для x - delta
                    y2 = FunctionHelper.calculateFunction2(x); // Расчет значения функции для x
                    y3 = FunctionHelper.calculateFunction2(x + delta); // Расчет значения функции для x + delta
                } else if (getLocalName().equals("agent3")) {
                    y1 = FunctionHelper.calculateFunction3(x - delta); // Расчет значения функции для x - delta
                    y2 = FunctionHelper.calculateFunction3(x); // Расчет значения функции для x
                    y3 = FunctionHelper.calculateFunction3(x + delta); // Расчет значения функции для x + delta
                } else {
                    y1 = FunctionHelper.calculateFunction1(x - delta); // Расчет значения функции для x - delta
                    y2 = FunctionHelper.calculateFunction1(x); // Расчет значения функции для x
                    y3 = FunctionHelper.calculateFunction1(x + delta); // Расчет значения функции для x + delta
                }

                // Отправка ответа
                ACLMessage reply = new ACLMessage(ACLMessage.INFORM); // Создаем сообщение с ответом
                reply.addReceiver(request.getSender()); // Добавляем отправителя запроса в список получателей
                reply.setContent(y1 + "," + y2 + "," + y3); // Устанавливаем содержимое ответа (значения функций)
                send(reply); // Отправляем ответ
            } else {
                block(); // Блокируем выполнение, если запрос не получен
            }
        }

        @Override
        public boolean done() {
            return false; // Поведение будет выполняться циклически
        }
    }
}