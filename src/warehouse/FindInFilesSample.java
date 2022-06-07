package warehouse;

public final class FindInFilesSample {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();
        warehouse.addFruits("peach", 3);
        warehouse.addFruits("pineapple", 5);
        warehouse.addFruits("mango", 1);
        warehouse.addFruits("orange", 5);

        boolean result = warehouse.takeFruit("orange");
        if (result) {
            System.out.println("This orange was delicious!");
        }

        warehouse.printAllFruits();
    }
}
