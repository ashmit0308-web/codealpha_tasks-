import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Stocktradingplatform {

    public static void main(String[] args) {
        new Stocktradingplatform().run();
    }

    private static final double INITIAL_BALANCE = 10000.0;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    
    static class Stock {
        final String symbol;
        final String companyName;
        double price;

        Stock(String symbol, String companyName, double price) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.price = price;
        }
    }

    
    static class Transaction {
        final String type; // BUY or SELL
        final String symbol;
        final int quantity;
        final double priceAtTransaction;
        final LocalDateTime timestamp;

        Transaction(String type, String symbol, int quantity, double priceAtTransaction) {
            this.type = type;
            this.symbol = symbol;
            this.quantity = quantity;
            this.priceAtTransaction = priceAtTransaction;
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return String.format("[%s] %-4s %3dx %-6s @ $%.2f",
                    timestamp.format(TIME_FORMAT), type, quantity, symbol, priceAtTransaction);
        }
    }

    
    static class Holding {
        int quantity;
        double avgBuyPrice;

        Holding(int quantity, double avgBuyPrice) {
            this.quantity = quantity;
            this.avgBuyPrice = avgBuyPrice;
        }
    }

    
    static class Portfolio {
        double cashBalance;
        final Map<String, Holding> holdings = new HashMap<>();
        final List<Transaction> history = new ArrayList<>();
        double realizedProfitLoss = 0.0;

        Portfolio(double startingCash) {
            this.cashBalance = startingCash;
        }

        void buy(Stock stock, int quantity) {
            double cost = stock.price * quantity;
            cashBalance -= cost;
            Holding h = holdings.get(stock.symbol);
            if (h == null) {
                holdings.put(stock.symbol, new Holding(quantity, stock.price));
            } else {
                double totalCost = (h.avgBuyPrice * h.quantity) + cost;
                h.quantity += quantity;
                h.avgBuyPrice = totalCost / h.quantity;
            }
            history.add(new Transaction("BUY", stock.symbol, quantity, stock.price));
        }

        void sell(Stock stock, int quantity) {
            double proceeds = stock.price * quantity;
            cashBalance += proceeds;
            Holding h = holdings.get(stock.symbol);
            if (h != null) {
                realizedProfitLoss += (stock.price - h.avgBuyPrice) * quantity;
                h.quantity -= quantity;
                if (h.quantity <= 0) holdings.remove(stock.symbol);
            }
            history.add(new Transaction("SELL", stock.symbol, quantity, stock.price));
        }

        double getHoldingsValue(Market market) {
            double total = 0;
            for (Map.Entry<String, Holding> entry : holdings.entrySet()) {
                Stock s = market.findStock(entry.getKey());
                if (s != null) total += s.price * entry.getValue().quantity;
            }
            return total;
        }
    }

    // ---------- Market ----------
    static class Market {
        final List<Stock> stocks = new ArrayList<>();
        final Random random = new Random();

        Market() {
            stocks.add(new Stock("AAPL", "Apple Inc.", 195.50));
            stocks.add(new Stock("GOOG", "Alphabet Inc.", 142.30));
            stocks.add(new Stock("AMZN", "Amazon.com Inc.", 178.90));
            stocks.add(new Stock("TSLA", "Tesla Inc.", 245.10));
            stocks.add(new Stock("MSFT", "Microsoft Corp.", 415.75));
        }

        Stock findStock(String symbol) {
            for (Stock s : stocks) {
                if (s.symbol.equalsIgnoreCase(symbol)) return s;
            }
            return null;
        }

        List<Stock> search(String keyword) {
            List<Stock> results = new ArrayList<>();
            String lower = keyword.toLowerCase();
            for (Stock s : stocks) {
                if (s.symbol.toLowerCase().contains(lower) || s.companyName.toLowerCase().contains(lower)) {
                    results.add(s);
                }
            }
            return results;
        }

        void simulateMarketMovement() {
            for (Stock s : stocks) {
                double changePercent = (random.nextDouble() * 6) - 3; // -3% to +3%
                s.price = Math.max(1, s.price * (1 + changePercent / 100));
            }
        }

        void printMarketData() {
            System.out.println("\n===== MARKET DATA =====");
            System.out.printf("%-8s %-20s %-10s%n", "Symbol", "Company", "Price");
            System.out.println("-".repeat(40));
            for (Stock s : stocks) {
                System.out.printf("%-8s %-20s $%-9.2f%n", s.symbol, s.companyName, s.price);
            }
        }
    }

    private final Market market = new Market();
    private final Portfolio portfolio = new Portfolio(INITIAL_BALANCE);
    private final Scanner scanner = new Scanner(System.in);
    private static final String SAVE_FILE = "portfolio.txt";

    

    private void run() {
        loadPortfolio();
        printBanner();
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> market.printMarketData();
                case 2 -> buyStock();
                case 3 -> sellStock();
                case 4 -> viewPortfolio();
                case 5 -> viewTransactionHistory();
                case 6 -> searchStock();
                case 7 -> {
                    market.simulateMarketMovement();
                    System.out.println("Market prices updated for this session.");
                }
                case 8 -> {
                    savePortfolio();
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice.\n");
            }
        }
        scanner.close();
    }

    private void printBanner() {
        System.out.println("====================================");
        System.out.println("      STOCK TRADING PLATFORM");
        System.out.println("====================================");
    }

    private void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. View Market Data");
        System.out.println("2. Buy Stocks");
        System.out.println("3. Sell Stocks");
        System.out.println("4. Portfolio Summary");
        System.out.println("5. Transaction History");
        System.out.println("6. Search Stock");
        System.out.println("7. Simulate Market Movement");
        System.out.println("8. Save & Exit");
    }

    private void buyStock() {
        market.printMarketData();
        System.out.print("Enter stock symbol to buy: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = market.findStock(symbol);
        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }
        int qty = readInt("Enter quantity: ");
        if (qty <= 0) {
            System.out.println("Quantity must be greater than 0.");
            return;
        }
        double cost = stock.price * qty;
        if (cost > portfolio.cashBalance) {
            System.out.println("Insufficient cash balance. Available: $" + String.format("%.2f", portfolio.cashBalance));
            return;
        }
        portfolio.buy(stock, qty);
        System.out.printf("Bought %d shares of %s for $%.2f%n", qty, symbol, cost);
    }

    private void sellStock() {
        System.out.print("Enter stock symbol to sell: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = market.findStock(symbol);
        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }
        Holding holding = portfolio.holdings.get(symbol);
        int owned = holding != null ? holding.quantity : 0;
        if (owned <= 0) {
            System.out.println("You don't own any shares of " + symbol);
            return;
        }
        int qty = readInt("Enter quantity to sell (you own " + owned + "): ");
        if (qty <= 0) {
            System.out.println("Quantity must be greater than 0.");
            return;
        }
        if (qty > owned) {
            System.out.println("You can't sell more than you own.");
            return;
        }
        double proceeds = stock.price * qty;
        double profitLoss = (stock.price - holding.avgBuyPrice) * qty;
        portfolio.sell(stock, qty);
        System.out.printf("Sold %d shares of %s for $%.2f (P/L: %s$%.2f)%n",
                qty, symbol, proceeds, profitLoss >= 0 ? "+" : "-", Math.abs(profitLoss));
    }

    private void viewPortfolio() {
        System.out.println("\n===== PORTFOLIO SUMMARY =====");
        System.out.printf("Cash balance: $%.2f%n", portfolio.cashBalance);
        if (portfolio.holdings.isEmpty()) {
            System.out.println("No stock holdings yet.");
        } else {
            System.out.printf("%-8s %-8s %-10s %-10s %-12s %-14s%n",
                    "Symbol", "Qty", "Avg Buy", "Current", "Value", "P/L");
            for (Map.Entry<String, Holding> entry : portfolio.holdings.entrySet()) {
                Holding h = entry.getValue();
                Stock s = market.findStock(entry.getKey());
                double currentPrice = s != null ? s.price : 0;
                double value = currentPrice * h.quantity;
                double pl = (currentPrice - h.avgBuyPrice) * h.quantity;
                System.out.printf("%-8s %-8d $%-9.2f $%-9.2f $%-11.2f %s$%-12.2f%n",
                        entry.getKey(), h.quantity, h.avgBuyPrice, currentPrice, value,
                        pl >= 0 ? "+" : "-", Math.abs(pl));
            }
        }
        double totalValue = portfolio.cashBalance + portfolio.getHoldingsValue(market);
        double overallGain = totalValue - INITIAL_BALANCE;
        double overallGainPercent = (overallGain / INITIAL_BALANCE) * 100;
        System.out.printf("Realized P/L (from closed trades): %s$%.2f%n",
                portfolio.realizedProfitLoss >= 0 ? "+" : "-", Math.abs(portfolio.realizedProfitLoss));
        System.out.printf("TOTAL PORTFOLIO VALUE: $%.2f%n", totalValue);
        System.out.printf("OVERALL GAIN/LOSS: %s$%.2f (%s%.2f%%)%n",
                overallGain >= 0 ? "+" : "-", Math.abs(overallGain),
                overallGainPercent >= 0 ? "+" : "-", Math.abs(overallGainPercent));
    }

    private void viewTransactionHistory() {
        System.out.println("\n===== TRANSACTION HISTORY =====");
        if (portfolio.history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        for (Transaction t : portfolio.history) {
            System.out.println(t);
        }
    }

    private void searchStock() {
        System.out.print("Enter symbol or company name keyword: ");
        String keyword = scanner.nextLine().trim();
        List<Stock> results = market.search(keyword);
        if (results.isEmpty()) {
            System.out.println("No matching stocks found.");
            return;
        }
        System.out.println("\n===== SEARCH RESULTS =====");
        System.out.printf("%-8s %-20s %-10s%n", "Symbol", "Company", "Price");
        for (Stock s : results) {
            System.out.printf("%-8s %-20s $%-9.2f%n", s.symbol, s.companyName, s.price);
        }
    }

    // ---------- File I/O persistence ----------
    private void savePortfolio() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            writer.println("CASH," + portfolio.cashBalance);
            writer.println("REALIZED_PL," + portfolio.realizedProfitLoss);
            for (Map.Entry<String, Holding> entry : portfolio.holdings.entrySet()) {
                Holding h = entry.getValue();
                writer.println("HOLDING," + entry.getKey() + "," + h.quantity + "," + h.avgBuyPrice);
            }
            for (Transaction t : portfolio.history) {
                writer.println("TXN," + t.type + "," + t.symbol + "," + t.quantity + "," +
                        t.priceAtTransaction + "," + t.timestamp);
            }
        } catch (IOException e) {
            System.out.println("Could not save portfolio: " + e.getMessage());
        }
    }

    private void loadPortfolio() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                switch (parts[0]) {
                    case "CASH" -> portfolio.cashBalance = Double.parseDouble(parts[1]);
                    case "REALIZED_PL" -> portfolio.realizedProfitLoss = Double.parseDouble(parts[1]);
                    case "HOLDING" -> portfolio.holdings.put(parts[1],
                            new Holding(Integer.parseInt(parts[2]), Double.parseDouble(parts[3])));
                    case "TXN" -> portfolio.history.add(new Transaction(parts[1], parts[2],
                            Integer.parseInt(parts[3]), Double.parseDouble(parts[4])));
                }
            }
            System.out.println("Loaded saved portfolio from " + SAVE_FILE);
        } catch (IOException e) {
            System.out.println("Could not load portfolio: " + e.getMessage());
        }
    }

    // ---------- Input helpers ----------
    private int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }
}
    

