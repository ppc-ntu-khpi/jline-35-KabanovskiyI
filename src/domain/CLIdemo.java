package domain;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import com.mybank.data.DataSource;
import com.mybank.reporting.CustomerReport;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.utils.*;
import org.fusesource.jansi.*;

public class CLIdemo {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private String[] commandsList;

    public void init() {
        commandsList = new String[]{"help", "customers", "customer", "report" ,"exit"};
    }

    public void run() {
        AnsiConsole.systemInstall(); // needed to support ansi on Windows cmd
        printWelcomeMessage();
        LineReaderBuilder readerBuilder = LineReaderBuilder.builder();
        List<Completer> completors = new LinkedList<Completer>();

        completors.add(new StringsCompleter(commandsList));
        readerBuilder.completer(new ArgumentCompleter(completors));

        LineReader reader = readerBuilder.build();

        String line;
        PrintWriter out = new PrintWriter(System.out);

        while ((line = readLine(reader, "")) != null) {
            if ("help".equals(line)) {
                printHelp();
            } else if ("customers".equals(line)) {
                AttributedStringBuilder a = new AttributedStringBuilder()
                        .append("\nThis is all of your ")
                        .append("customers", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                        .append(":");

                System.out.println(a.toAnsi());
                if (Bank.getNumberOfCustomers() > 0) {
                    System.out.println("\nLast name\tFirst Name\tBalance");
                    System.out.println("---------------------------------------");
                    for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                        Customer c = Bank.getCustomer(i);
                        if (c.getNumberOfAccounts() > 0) {
                            System.out.println(c.getLastName() + "\t\t" + c.getFirstName() + "\t\t$" + c.getAccount(0).getBalance());
                        } else {
                            System.out.println(c.getLastName() + "\t\t" + c.getFirstName() + "\t\t(no accounts)");
                        }
                    }
                } else {
                    System.out.println(ANSI_RED + "Your bank has no customers!" + ANSI_RESET);
                }

            } else if (line.startsWith("customer")) {
                try {
                    int custNo = 0;
                    if (line.length() > 8) {
                        String strNum = line.split(" ")[1];
                        if (strNum != null) {
                            custNo = Integer.parseInt(strNum);
                        }
                    }

                    Customer cust = Bank.getCustomer(custNo);
                    String accType = cust.getAccount(0) instanceof CheckingAccount ? "Checking" : "Savings";

                    AttributedStringBuilder a = new AttributedStringBuilder()
                            .append("\nThis is detailed information about customer #")
                            .append(Integer.toString(custNo), AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                            .append("!");

                    System.out.println(a.toAnsi());

                    System.out.println("\nLast name\tFirst Name\tAccount Type\tBalance");
                    System.out.println("-------------------------------------------------------");
                    System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\t" + accType + "\t$" + cust.getAccount(0).getBalance());
                } catch (Exception e) {
                    System.out.println(ANSI_RED + "ERROR! Wrong customer number!" + ANSI_RESET);
                }
            } else if ("exit".equals(line)) {
                System.out.println("Exiting application");
                return;
            }else if ("report".equals(line)){
                CustomerReport report = new CustomerReport();
                report.generateReport();
            } else {
                System.out.println(ANSI_RED + "Invalid command, For assistance press TAB or type \"help\" then hit ENTER." + ANSI_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private void printWelcomeMessage() {
        System.out.println("\nWelcome to " + ANSI_GREEN + " MyBank Console Client App" + ANSI_RESET + "! \nFor assistance press TAB or type \"help\" then hit ENTER.");
    }

    private void printHelp() {
        System.out.println("help\t\t\t- Show help");
        System.out.println("customer\t\t- Show list of customers");
        System.out.println("customer 'index'\t- Show customer details");
        System.out.println("exit\t\t\t- Exit the app");
    }

    private String readLine(LineReader reader, String promtMessage) {
        try {
            return reader.readLine(promtMessage + ANSI_YELLOW + "\nbank> " + ANSI_RESET).trim();
        } catch (UserInterruptException | EndOfFileException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            DataSource dataSource = new DataSource("data/test.dat");
            dataSource.loadData();
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return;
        }

        CLIdemo shell = new CLIdemo();
        shell.init();
        shell.run();
    }

}
