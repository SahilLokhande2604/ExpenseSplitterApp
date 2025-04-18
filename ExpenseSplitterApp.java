import java.util.*;

class User {
    String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
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
                continue; // skip payer
            }

            netBalances.put(user, netBalances.getOrDefault(user, 0) - share);
            netBalances.put(paidBy, netBalances.getOrDefault(paidBy, 0) + share);

            debtMap.put(user, new HashMap<>());
        }
    }

    public void simplifyDebts() {
        // IMPORTANT: make a copy of netBalances so original remains unchanged
        Map<User, Integer> tempBalances = new HashMap<>(netBalances);

        PriorityQueue<Map.Entry<User, Integer>> positive = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
        PriorityQueue<Map.Entry<User, Integer>> negative = new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<User, Integer> entry : tempBalances.entrySet()) {
            if (entry.getValue() > 0)
                positive.offer(entry);
            else if (entry.getValue() < 0)
                negative.offer(entry);
        }

        while (!positive.isEmpty() && !negative.isEmpty()) {
            Map.Entry<User, Integer> creditor = positive.poll();
            Map.Entry<User, Integer> debtor = negative.poll();
            int settledAmount = Math.min(creditor.getValue(), -debtor.getValue());

            User creditorUser = creditor.getKey();
            User debtorUser = debtor.getKey();

            // Update debt mapping

            debtMap.get(debtorUser).put(creditorUser, settledAmount);
            logs.add(debtorUser + " will pay " + settledAmount + " to " + creditorUser);

            int creditorNewBalance = creditor.getValue() - settledAmount;
            int debtorNewBalance = debtor.getValue() + settledAmount;

            if (creditorNewBalance > 0)
                positive.offer(new AbstractMap.SimpleEntry<>(creditorUser, creditorNewBalance));
            if (debtorNewBalance < 0)
                negative.offer(new AbstractMap.SimpleEntry<>(debtorUser, debtorNewBalance));
        }
    }

    public void makePayment(User from, User to, int amount) {
        netBalances.put(from, netBalances.getOrDefault(from, 0) - amount);
        netBalances.put(to, netBalances.getOrDefault(to, 0) + amount);

        Map<User, Integer> fromDebts = debtMap.getOrDefault(from, new HashMap<>());
        int owed = fromDebts.getOrDefault(to, 0);

        if (owed >= amount) {
            fromDebts.put(to, owed - amount);
            if (fromDebts.get(to) == 0) {
                fromDebts.remove(to);
            }
        } else {
            fromDebts.remove(to);
            Map<User, Integer> toDebts = debtMap.getOrDefault(to, new HashMap<>());
            toDebts.put(from, toDebts.getOrDefault(from, 0) + (amount - owed));
        }

        logs.add(from + " paid " + amount + " to " + to);
    }

    public void showBalances(User user) {
        System.out.println("Balances for user: " + user + " -> Net balance: " + netBalances.getOrDefault(user, 0));
    }

    public void showAllDebts() {
        System.out.println("Detailed Debts in Group " + groupName + ":");
        for (User from : debtMap.keySet()) {
            for (Map.Entry<User, Integer> entry : debtMap.get(from).entrySet()) {
                if (entry.getValue() > 0) {
                    System.out.println("  " + from + " will pay Rs. " + entry.getValue() + " to " + entry.getKey());
                }
            }
        }
    }

    public void showLogs() {
        System.out.println("Transaction Log for Group " + groupName + ":");
        for (String log : logs) {
            System.out.println("  - " + log);
        }
    }
}

public class ExpenseSplitterApp {
    static Scanner sc = new Scanner(System.in);
    static Map<String, User> users = new HashMap<>();
    static Map<String, Group> groups = new HashMap<>();

    public static void main(String[] args) {
        while (true) {
            System.out.println(
                    "\n1. Create User\n2. Create Group\n3. Add User to Group\n4. Add Expense\n5. Make Payment\n6. View Balances\n7. View Logs\n8. View Debts\n9. Run Test Case\n10. Exit");
            System.out.print("Choose option: ");
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
                case 10 -> System.exit(0);
            }
        }
    }

    static void createUser() {
        System.out.print("Enter username: ");
        String name = sc.nextLine();
        users.put(name, new User(name));
    }

    static void createGroup() {
        System.out.print("Enter group name: ");
        String name = sc.nextLine();
        groups.put(name, new Group(name));
    }

    static void addUsersToGroup() {
        System.out.print("Enter group name: ");
        String gName = sc.nextLine();
        boolean flag = true;
        while (flag) {
            System.out.print("Enter user name: ");
            String uName = sc.nextLine();
            if (groups.containsKey(gName) && users.containsKey(uName)) {
                groups.get(gName).addUser(users.get(uName));
            }
            System.out.println("To enter new user enter 1 else enter 0");
            String flagVal = sc.nextLine();
            if (flagVal.equals("0")) {
                flag = false;
            }
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
        group.simplifyDebts(); // Now it won't disturb real netBalances
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
        System.out.println("Running test case...");
        User a = new User("0");
        User b = new User("1");
        User c = new User("2");
        User d = new User("3");
        User e = new User("4");
        users.put("0", a);
        users.put("1", b);
        users.put("2", c);
        users.put("3", d);
        users.put("4", e);

        Group trip = new Group("Trip");
        trip.addUser(a);
        trip.addUser(b);
        trip.addUser(c);
        trip.addUser(d);
        trip.addUser(e);
        groups.put("Trip", trip);

        Map<User, Integer> split1 = new HashMap<>();
        split1.put(a, 200);
        split1.put(b, 0);
        split1.put(c, 400);
        trip.addExpense(a, 600, split1);

        Map<User, Integer> split2 = new HashMap<>();
        split2.put(a, 100);
        split2.put(b, 100);
        split2.put(c, 100);
        trip.addExpense(b, 300, split2);

        Map<User, Integer> split3 = new HashMap<>();
        split3.put(a, 100);
        split3.put(b, 0);
        split3.put(c, 0);
        trip.addExpense(c, 100, split3);

        trip.simplifyDebts();

        trip.showBalances(a);
        trip.showBalances(b);
        trip.showBalances(c);
        trip.showLogs();
        trip.showAllDebts();
    }
}
