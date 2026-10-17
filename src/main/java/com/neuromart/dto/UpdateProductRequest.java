package com.neuromart.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private String name;
    private String description;
    private Double price;
    private String category;
    private Integer stock;
    private List<String> images;

    @JsonSetter("image")
    public void setImage(Object image) {
        if (image == null) {
            this.images = null;
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
        this.images = images;
    }
}
