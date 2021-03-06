package server.Database;

import server.Consts.Answer;
import server.Database.DatabaseConnector.DataBase;
import server.FactoryGson.GsonDateFormatGetter;
import server.FactoryGson.GsonGetter;
import server.Models.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductManager {

    private static Statement stmt;
    private static ProductManager databaseManager;

    public static ProductManager getDatabaseManager() {
        if (databaseManager == null) databaseManager = new ProductManager();
        return databaseManager;
    }

    private ProductManager() {
        stmt = DataBase.getDatabase().getStmt();
    }



    public List<Product> ShowGoods() {
        String query = "SELECT (SELECT sum(o.countInOrder)\n" +
                "FROM test.order o \n" +
                "inner join test.size s on s.sizeID = o.sizeID\n" +
                "inner join test.product p on p.productId = s.productId\n" +
                "WHERE p.name = test.product.name\n" +
                "), test.product.*, test.material.*, test.size.* FROM test.product\n" +
                "inner join test.material on test.material.materialId = test.product.materialId\n" +
                "left join test.review on test.review.productId = test.product.productId\n" +
                "left join test.size on test.product.productId = test.size.productId";
        try {
            ResultSet rs = stmt.executeQuery(query);
            List<Product> goods = new ArrayList<>();
            while (rs.next()) {
                if (goods.size() == 0 || goods.get(goods.size() - 1).getProductId() != rs.getInt("productId")) {
                    goods.add(new Product(
                            rs.getInt("materialId"),
                            rs.getString("material"),
                            rs.getString("color"),
                            rs.getString("pattern"),
                            rs.getInt("productId"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("type"),
                            rs.getInt(1)));
                }
                if (rs.getInt("sizeId") != 0) goods.get(goods.size() - 1).addSize(new Size(
                        rs.getInt("sizeId"),
                        rs.getString("size"),
                        rs.getInt("count")));

            }

            return goods;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }
    public void SetRate(String message)
    {
        Review review = new GsonDateFormatGetter().getGson().fromJson(message, Review.class);
        String query = "INSERT INTO test.review (productId, userId, rate) VALUES (" + review.getProduct().getProductId() +
                ", " + review.getUser().getUserId() + ", " + review.getRate() + ");";
        try {
            stmt.executeUpdate(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public List<Review> GetRates()
    {
        String query = "SELECT (SELECT avg(r.rate)\n" +
                "FROM test.review r \n" +
                "WHERE r.productId = test.review.productId), test.review.* \n" +
                "FROM test.review";
        try {
            ResultSet rs = stmt.executeQuery(query);
            List<Review> reviews = new ArrayList<>();
            while (rs.next())
            {
                reviews.add(Review.reviewBuilder().reviewId(rs.getInt("reviewId")).rate(rs.getInt("rate"))
                        .product(Product.productBuilder().productId(rs.getInt("productId")).build()).user(
                                User.userBuilder().userId(rs.getInt("userId")).build()).averageRate(rs.getDouble(1)).build());
            }
            return reviews;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }
    public List<Review> GetProductRates(String message)
    {
        Product product = new GsonGetter().getGson().fromJson(message, Product.class);
        String query = "SELECT (SELECT avg(r.rate)\n" +
                "FROM test.review r \n" +
                "WHERE r.productId = test.review.productId), test.review.* \n" +
                "FROM test.review\n"+
                "WHERE test.review.productId = " + product.getProductId();
        try {
            ResultSet rs = stmt.executeQuery(query);
            List<Review> reviews = new ArrayList<>();
            while (rs.next())
            {
                reviews.add(Review.reviewBuilder().reviewId(rs.getInt("reviewId")).rate(rs.getInt("rate"))
                        .product(Product.productBuilder().productId(rs.getInt("productId")).build()).user(
                                User.userBuilder().userId(rs.getInt("userId")).build()).averageRate(rs.getDouble(1)).build());
            }
            return reviews;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public List<Product> ShowGoodsPopularity()
    {
        String query = "SELECT (SELECT sum(o.countInOrder)\n" +
                "FROM test.order o \n" +
                "inner join test.size s on s.sizeID = o.sizeID\n" +
                "inner join test.product p on p.productId = s.productId\n" +
                "WHERE p.name = test.product.name\n" +
                "), test.product.*, test.material.*, test.size.*\n" +
                "FROM test.product\n" +
                "inner join test.material on test.product.materialId = test.material.materialId\n" +
                "inner join test.size on test.product.productId = test.size.productId\n" +
                "order by (SELECT sum(o.countInOrder)\n" +
                "FROM test.order o \n" +
                "inner join test.size s on s.sizeID = o.sizeID\n" +
                "inner join test.product p on p.productId = s.productId\n" +
                "WHERE p.name = test.product.name\n" +
                ") desc;";
        try {
            ResultSet rs = stmt.executeQuery(query);
            List<Product> goods = new ArrayList<>();
            while (rs.next()) {
                if (goods.size() == 0 || goods.get(goods.size() - 1).getProductId() != rs.getInt("materialId")) {
                    goods.add(new Product(
                            rs.getInt("materialId"),
                            rs.getString("material"),
                            rs.getString("color"),
                            rs.getString("pattern"),
                            rs.getInt("productId"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("type"),
                            rs.getInt(1)));
                }
                if (rs.getInt("sizeId") != 0) goods.get(goods.size() - 1).addSize(new Size(
                        rs.getInt("sizeId"),
                        rs.getString("size"),
                        rs.getInt("count")));

            }

            return goods;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public Product ShowProduct(String message) {
        Product product = new GsonGetter().getGson().fromJson(message, Product.class);
        String query = "SELECT (SELECT sum(o.countInOrder)\n" +
                "FROM test.order o \n" +
                "inner join test.size s on s.sizeID = o.sizeID\n" +
                "inner join test.product p on p.productId = s.productId\n" +
                "WHERE p.name = test.product.name\n" +
                "), test.product.*, test.material.*, test.size.* FROM test.product\n" +
                "inner join test.material on test.material.materialId = test.product.materialId\n" +
                "left join test.size on test.product.productId = test.size.productId\n" +
                "WHERE test.product.productId = " + product.getProductId() + ";";
        try {
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            Product selectedProduct = new Product(
                    rs.getInt("materialId"),
                    rs.getString("material"),
                    rs.getString("color"),
                    rs.getString("pattern"),
                    rs.getInt("productId"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getString("type"),
                    rs.getInt(1));
            selectedProduct.addSize(new Size(
                    rs.getInt("sizeId"),
                    rs.getString("size"),
                    rs.getInt("count")));
            while (rs.next()) {
                selectedProduct.addSize(new Size(
                        rs.getInt("sizeId"),
                        rs.getString("size"),
                        rs.getInt("count")));
            }

            return selectedProduct;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public String editProduct(String message) {
        Product product = new GsonGetter().getGson().fromJson(message, Product.class);
        String query = "UPDATE test.product\n" +
                "inner join test.material on test.material.materialId = test.product.materialId\n" +
                "set test.product.name = '" + product.getName() + "', test.product.description = '" + product.getDescription() + "', " +
                "test.product.price = " + product.getPrice() + ", test.product.type = '" + product.getType() + "',\n" +
                "test.material.color= '" + product.getColor() + "', test.material.material = '" + product.getMaterial() + "', " +
                "test.material.pattern= '" + product.getPattern() + "'\n" +
                "where test.product.productId = " + product.getProductId() + ";";

        try {
            stmt.executeUpdate(query);
            query = "delete from test.size where test.size.productId = " + product.getProductId() + ";";
            stmt.executeUpdate(query);
            SizeManager.getDatabaseManager().getInsertSizeQuery(product.getSizes(), product.getProductId());
            return Answer.SUCCESS.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return Answer.ERROR.toString();
        }
    }

    public void addProduct(String message) throws SQLException {

        Product product = new GsonGetter().getGson().fromJson(message, Product.class);
        String query = "INSERT INTO test.material (material, color, pattern)\n" +
                "VALUES ('" + product.getMaterial() + "', '" + product.getColor() + "', '" + product.getPattern() + "');";
        stmt.executeUpdate(query);
        query = "INSERT INTO test.product (name, description, price, type, materialId)\n" +
                "VALUES ('" + product.getName() + "', '" + product.getDescription() + "', " +
                product.getPrice() + ", '" + product.getType() + "', (select max(materialId)\n" +
                "from test.material));";
        stmt.executeUpdate(query);
        if (product.getSizes().size() != 0) {
            query = "select max(productId) from test.product";
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            SizeManager.getDatabaseManager().getInsertSizeQuery(product.getSizes(), rs.getInt(1));
        }
    }

    public void deleteProduct(String message) throws SQLException {
        Product product = new GsonGetter().getGson().fromJson(message, Product.class);
        String query = "DELETE FROM test.product WHERE test.product.productId = " + product.getProductId();
        stmt.executeUpdate(query);

    }
    public List<Product> ShowFilterGoods(String message) {
        SortConfiguration filter = new GsonDateFormatGetter().getGson().fromJson(message, SortConfiguration.class);
        String query = "SELECT (SELECT sum(o.countInOrder)\n" +
                "FROM test.order o \n" +
                "inner join test.size s on s.sizeID = o.sizeID\n" +
                "inner join test.product p on p.productId = s.productId\n" +
                "WHERE p.name = test.product.name\n" +
                "), test.product.*, test.material.*, test.size.* FROM test.product\n" +
                "inner join test.material on test.material.materialId = test.product.materialId\n" +
                "left join test.size on test.product.productId = test.size.productId\n" +
                "WHERE " + filter.getSortColumn() + " LIKE '%" + filter.getSortValue() + "%'";
        try {
            ResultSet rs = stmt.executeQuery(query);
            List<Product> goods = new ArrayList<>();
            while (rs.next()) {
                if (goods.size() == 0 || goods.get(goods.size() - 1).getProductId() != rs.getInt("productId")) {
                    goods.add(new Product(
                            rs.getInt("materialId"),
                            rs.getString("material"),
                            rs.getString("color"),
                            rs.getString("pattern"),
                            rs.getInt("productId"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getString("type"),
                            rs.getInt(1)));
                }
                if (rs.getInt("sizeId") != 0) goods.get(goods.size() - 1).addSize(new Size(
                        rs.getInt("sizeId"),
                        rs.getString("size"),
                        rs.getInt("count")));

            }

            return goods;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }
}
