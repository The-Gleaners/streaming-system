package gleaners.usecase;

import gleaners.domain.enums.Field;
import gleaners.domain.Product;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class Parser
{
    private static final String DELIMITER = "\\t";
    private List<Field> fieldList;

    public void setFieldKey(String fields) {
        fieldList = Arrays.stream(fields.trim().split(DELIMITER, -1))
            .map(field -> Field.getFieldKeyMap().get(field))
            .collect(Collectors.toList());
    }

    public Product produceProduct(String line) {
        Product product = new Product();
        try {
            List<String> valueList = Arrays.asList(line.split("\\t", -1));
            if(fieldList.size() != valueList.size()) {
                throw new Exception("key와 value의 필드 개수가 동일하지 않습니다. field key size : "
                    + fieldList.size() + ", value size : " + valueList.size());
            }

            for(int i = 0; i < valueList.size(); i++) {
                productSetter(product, fieldList.get(i), valueList.get(i));
            }
        } catch(Exception e) {
            log.error("Product 생성 에러 : {}", e.getMessage());
        }
        return product;
    }

    public boolean isEmptyFieldList() {
        return fieldList.isEmpty();
    }

    private void productSetter(Product product, Field field, String value) {
        switch (field) {
            case ID -> product.setId(value);
            case TITLE -> product.setTitle(value);
            case PRICE_PC -> product.setPricePc(Integer.parseInt(value));
            case LINK -> product.setLink(value);
            case IMAGE_LINK -> product.setImageLink(value);
            case CATEGORY_NAME1 -> product.setCategoryName1(value);
            case SHIPPING -> product.setShipping(value);
            case CLASS -> product.setFlag(value);
        }
    }
}
