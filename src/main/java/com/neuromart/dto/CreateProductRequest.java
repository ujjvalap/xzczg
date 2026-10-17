package com.neuromart.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Please Enter Product Name")
    private String name;

    @NotBlank(message = "Please Enter Product Description")
    private String description;

    @NotNull(message = "Please Enter Product Price")
    private Double price;

    @NotBlank(message = "Please Enter Product Category")
    private String category;

    private Integer stock = 1;

    private List<String> images = new ArrayList<>();

    @JsonSetter("image")
    @SuppressWarnings("unchecked")
    public void setImage(Object image) {
        if (image == null) {
            this.images = new ArrayList<>();
        } else if (image instanceof String) {
            this.images = new ArrayList<>();
            this.images.add((String) image);
        } else if (image instanceof List) {
            this.images = new ArrayList<>();
            for (Object item : (List<?>) image) {
                if (item != null) {
                    this.images.add(item.toString());
                }
            }
        }
    }

    @JsonSetter("images")
    public void setImagesList(List<String> images) {
        if (images != null) {
            this.images = images;
        }
    }
}
