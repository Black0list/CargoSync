package com.spring.logitrack.mapper;

import com.spring.logitrack.dto.product.ProductCreateDTO;
import com.spring.logitrack.dto.product.ProductResponseDTO;
import com.spring.logitrack.entity.Product;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "imageUrls", source = "imageUrls", qualifiedByName = "mapMultipartFiles")
    Product toEntity(ProductCreateDTO dto);

    ProductResponseDTO toResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name", conditionExpression = "java(isNotBlank(dto.getName()))")
    @Mapping(target = "description", conditionExpression = "java(isNotBlank(dto.getDescription()))")
    @Mapping(target = "sku", conditionExpression = "java(isNotBlank(dto.getSku()))")
    @Mapping(target = "unit", conditionExpression = "java(isNotBlank(dto.getUnit()))")
    @Mapping(target = "imageUrls", source = "imageUrls", qualifiedByName = "mapMultipartFiles")
    void patch(@MappingTarget Product product, ProductCreateDTO dto);

    default boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Named("mapMultipartFiles")
    default List<String> mapMultipartFiles(List<MultipartFile> files) {
        if (files == null) return null;
        return files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());
    }
}
