package gleaners.domain;

public class Product {
    private String response;

    public Product(String response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "Product{" +
                "response='" + response + '\'' +
                '}';
    }
}
