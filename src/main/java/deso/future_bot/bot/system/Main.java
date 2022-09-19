package deso.future_bot.bot.system;

import deso.future_bot.bot.modes.Backtesting;
import deso.future_bot.bot.modes.Collection;
import deso.future_bot.bot.modes.Simulation;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        System.out.println("---Startup...");
        //Program config.
        try {
            ConfigSetup.readConfig();
        } catch (ExceptionInInitializerError cause) {
            if (cause.getCause() != null) {
                if (cause.getCause().getMessage().toLowerCase().contains("banned")) {
                    long bannedTime = Long.parseLong(cause.getCause().getMessage().split("until ")[1].split("\\.")[0]);
                    System.out.println("\nIP Banned by Binance API until " + Formatter.formatDate(bannedTime) + " (" + Formatter.formatDuration(bannedTime - System.currentTimeMillis()) + ")");
                }
            } else {
                System.out.println("---Error during startup: ");
                cause.printStackTrace();
            }
            new Scanner(System.in).next();
            System.exit(3);
        }
        while (true) {
            Scanner sc = new Scanner(System.in);
            while (true) {
                try {
                    //TODO: Change mode selection to single character
                    System.out.println("Enter bot mode (live, simulation, backtesting, collection)");
                    Mode.set(Mode.valueOf(sc.nextLine().toUpperCase()));
                    break;
                } catch (Exception e) {
                    Mode.set(Mode.BACKTESTING);
                    break;
                }
            }
            System.out.println("\n---Entering " + Mode.get().name().toLowerCase() + " mode");


            if (Mode.get() == Mode.COLLECTION) {
                Collection.startCollection(); //Init collection mode.
            } else {
                long startTime = System.nanoTime();
                switch (Mode.get()) {
                    case SIMULATION:
                        Simulation.init(); //Init simulation mode.
                        break;
                    case BACKTESTING:
                        Backtesting.startBackTesting(); //Init Backtesting mode.
                        break;
                }
                long endTime = System.nanoTime();
                double time = (endTime - startTime) / 1.e9;

                System.out.println("---" + (Mode.get().equals(Mode.BACKTESTING) ? "Backtesting" : "Setup") + " finished (" + Formatter.formatDecimal(time) + " s)\n");
                while (Mode.get().equals(Mode.BACKTESTING)) {
                    System.out.println("Type \"quit\" to quit");
                    System.out.println("Type \"modes\" to got back to mode selection.");
                    String s = sc.nextLine();
                    if (s.equalsIgnoreCase("quit")) {
                        System.exit(0);
                        break;
                    } else if (s.equalsIgnoreCase("modes")) {
                        break;
                    }
                }
            }
        }
    }
}