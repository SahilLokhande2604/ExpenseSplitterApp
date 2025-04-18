import java.util.*;

class User {
    String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}

class Group {
    String groupName;
    Set<User> members = new HashSet<>();
    Map<User, Integer> netBalances = new HashMap<>();
    Map<User, Map<User, Integer>> debtMap = new HashMap<>();
    List<String> logs = new ArrayList<>();

    public Group(String groupName) {
        this.groupName = groupName;
    }

    public void addUser(User user) {
        members.add(user);
        netBalances.putIfAbsent(user, 0);
        debtMap.putIfAbsent(user, new HashMap<>());
    }

    public void addExpense(User paidBy, int totalAmount, Map<User, Integer> shares) {
        for (Map.Entry<User, Integer> entry : shares.entrySet()) {
            User user = entry.getKey();
            int share = entry.getValue();

            if (user.equals(paidBy)) {
                debtMap.put(user, new HashMap<>());
                continue;
            }

            netBalances.put(user, netBalances.getOrDefault(user, 0) - share);
            netBalances.put(paidBy, netBalances.getOrDefault(paidBy, 0) + share);

            debtMap.put(user, new HashMap<>());
        }
    }

    public void simplifyDebts() {
        Map<User, Integer> tempBalances = new HashMap<>(netBalances);
        PriorityQueue<Map.Entry<User, Integer>> positive = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
        PriorityQueue<Map.Entry<User, Integer>> negative = new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<User, Integer> entry : tempBalances.entrySet()) {
            if (entry.getValue() > 0) positive.offer(entry);
            else if (entry.getValue() < 0) negative.offer(entry);
        }

        while (!positive.isEmpty() && !negative.isEmpty()) {
            Map.Entry<User, Integer> creditor = positive.poll();
            Map.Entry<User, Integer> debtor = negative.poll();
            int settledAmount = Math.min(creditor.getValue(), -debtor.getValue());

            User creditorUser = creditor.getKey();
            User debtorUser = debtor.getKey();

            debtMap.get(debtorUser).put(creditorUser, settledAmount);
            logs.add(debtorUser + " will pay Rs. " + settledAmount + " to " + creditorUser);

            int creditorNewBalance = creditor.getValue() - settledAmount;
            int debtorNewBalance = debtor.getValue() + settledAmount;

            if (creditorNewBalance > 0) positive.offer(new AbstractMap.SimpleEntry<>(creditorUser, creditorNewBalance));
            if (debtorNewBalance < 0) negative.offer(new AbstractMap.SimpleEntry<>(debtorUser, debtorNewBalance));
        }
    }

    public void makePayment(User from, User to, int amount) {
        netBalances.put(from, netBalances.getOrDefault(from, 0) - amount);
        netBalances.put(to, netBalances.getOrDefault(to, 0) + amount);

        Map<User, Integer> fromDebts = debtMap.getOrDefault(from, new HashMap<>());
        int owed = fromDebts.getOrDefault(to, 0);

        if (owed >= amount) {
            fromDebts.put(to, owed - amount);
            if (fromDebts.get(to) == 0) fromDebts.remove(to);
        } else {
            fromDebts.remove(to);
            Map<User, Integer> toDebts = debtMap.getOrDefault(to, new HashMap<>());
            toDebts.put(from, toDebts.getOrDefault(from, 0) + (amount - owed));
        }

        logs.add(from + " paid Rs. " + amount + " to " + to);
    }

    public void showBalances(User user) {
        System.out.println(user + " Net balance: Rs. " + netBalances.getOrDefault(user, 0));
    }

    public void showAllDebts() {
        System.out.println("\n================ Detailed Debts ================");
        for (User from : debtMap.keySet()) {
            for (Map.Entry<User, Integer> entry : debtMap.get(from).entrySet()) {
                if (entry.getValue() > 0) {
                    System.out.println(from + " will pay Rs. " + entry.getValue() + " to " + entry.getKey());
                }
            }
        }
        System.out.println("=================================================\n");
    }

    public void showLogs() {
        System.out.println("\n================ Transaction Logs ================");
        for (String log : logs) {
            System.out.println("- " + log);
        }
        System.out.println("===================================================\n");
    }
}

public class ExpenseSplitterApp {
    static Scanner sc = new Scanner(System.in);
    static Map<String, User> users = new HashMap<>();
    static Map<String, Group> groups = new HashMap<>();

    public static void main(String[] args) {
        while (true) {
            showMenu();
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> createUser();
                case 2 -> createGroup();
                case 3 -> addUsersToGroup();
                case 4 -> addExpense();
                case 5 -> makePayment();
                case 6 -> viewBalances();
                case 7 -> viewLogs();
                case 8 -> viewDebts();
                case 9 -> runTestCase();
                case 10 -> exitApp();
                default -> System.out.println("\u001B[31mInvalid option!\u001B[0m");
            }
            promptEnterKey();
        }
    }

    static void showMenu() {
        System.out.println("\n================ Expense Splitter ==================");
        System.out.println("1. Create User");
        System.out.println("2. Create Group");
        System.out.println("3. Add User to Group");
        System.out.println("4. Add Expense");
        System.out.println("5. Make Payment");
        System.out.println("6. View Balances");
        System.out.println("7. View Logs");
        System.out.println("8. View Debts");
        System.out.println("9. Run Sample Test Case");
        System.out.println("10. Exit");
        System.out.println("====================================================\n");
        System.out.print("Enter your choice: ");
        
    }

    static void createUser() {
        System.out.print("Enter username: ");
        String name = sc.nextLine();
        users.put(name, new User(name));
        System.out.println("\u001B[32mUser created successfully!\u001B[0m");
    }

    static void createGroup() {
        System.out.print("Enter group name: ");
        String name = sc.nextLine();
        groups.put(name, new Group(name));
        System.out.println("\u001B[32mGroup created successfully!\u001B[0m");
    }

    static void addUsersToGroup() {
        System.out.print("Enter group name: ");
        String gName = sc.nextLine();
        if (!groups.containsKey(gName)) {
            System.out.println("\u001B[31mGroup not found!\u001B[0m");
            return;
        }
        Group group = groups.get(gName);

        boolean adding = true;
        while (adding) {
            System.out.print("Enter user name: ");
            String uName = sc.nextLine();
            if (users.containsKey(uName)) {
                group.addUser(users.get(uName));
                System.out.println("User added to group.");
            } else {
                System.out.println("\u001B[31mUser not found!\u001B[0m");
            }
            System.out.print("Add another user? (y/n): ");
            String ans = sc.nextLine();
            adding = ans.equalsIgnoreCase("y");
        }
    }

    static void addExpense() {
        System.out.print("Group name: ");
        Group group = groups.get(sc.nextLine());
        System.out.print("Paid by (user name): ");
        User paidBy = users.get(sc.nextLine());
        System.out.print("Total amount: ");
        int amount = sc.nextInt();
        sc.nextLine();

        Map<User, Integer> split = new HashMap<>();
        for (User u : group.members) {
            System.out.print("Share for " + u.name + ": ");
            int share = sc.nextInt();
            split.put(u, share);
        }
        sc.nextLine();
        group.addExpense(paidBy, amount, split);
        group.simplifyDebts();
        System.out.println("\u001B[32mExpense added and debts simplified.\u001B[0m");
    }

    static void makePayment() {
        System.out.print("Group name: ");
        Group group = groups.get(sc.nextLine());
        System.out.print("From (user): ");
        User from = users.get(sc.nextLine());
        System.out.print("To (user): ");
        User to = users.get(sc.nextLine());
        System.out.print("Amount: ");
        int amount = sc.nextInt();
        sc.nextLine();
        group.makePayment(from, to, amount);
        System.out.println("\u001B[32mPayment done successfully!\u001B[0m");
    }

    static void viewBalances() {
        System.out.print("Group name: ");
        Group group = groups.get(sc.nextLine());
        for (User user : group.members) {
            group.showBalances(user);
        }
    }

    static void viewLogs() {
        System.out.print("Group name: ");
        Group group = groups.get(sc.nextLine());
        group.showLogs();
    }

    static void viewDebts() {
        System.out.print("Group name: ");
        Group group = groups.get(sc.nextLine());
        group.showAllDebts();
    }

    static void runTestCase() {
        System.out.println("\u001B[34mRunning Sample Test Case...\u001B[0m");
        User a = new User("A");
        User b = new User("B");
        User c = new User("C");
        users.put("A", a);
        users.put("B", b);
        users.put("C", c);

        Group trip = new Group("Trip");
        trip.addUser(a);
        trip.addUser(b);
        trip.addUser(c);
        groups.put("Trip", trip);

        Map<User, Integer> split1 = new HashMap<>();
        split1.put(a, 100);
        split1.put(b, 100);
        split1.put(c, 100);
        trip.addExpense(a, 300, split1);

        trip.simplifyDebts();
        trip.showBalances(a);
        trip.showBalances(b);
        trip.showBalances(c);
        trip.showLogs();
        trip.showAllDebts();
    }

    static void exitApp() {
        System.out.println("\u001B[32mThank you for using Expense Splitter!\u001B[0m");
        System.exit(0);
    }

    static void promptEnterKey() {
        System.out.println("\nPress ENTER to continue...");
        sc.nextLine();
    }
}
