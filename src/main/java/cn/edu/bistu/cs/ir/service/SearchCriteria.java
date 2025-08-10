package cn.edu.bistu.cs.ir.service;

import cn.edu.bistu.cs.ir.model.AgeGroup;
import cn.edu.bistu.cs.ir.model.Continent;
import cn.edu.bistu.cs.ir.model.WeightClass;

/**
 * 检索条件对象，用于封装多个检索参数
 * 支持组合条件检索，包括年龄范围、体重范围检索、模糊匹配检索
 * 
 * @author zhaxijiancuo
 */
public class SearchCriteria {
    
    private String keyword;           // 关键词
    private String fuzzyKeyword;      // 模糊关键词
    private Double similarity;        // 相似度阈值
    private AgeGroup ageGroup;        // 年龄组别
    private Integer minAge;           // 最小年龄
    private Integer maxAge;           // 最大年龄
    private WeightClass weightClass;  // 体重级别
    private Double minWeight;         // 最小体重
    private Double maxWeight;         // 最大体重
    private Continent continent;      // 大洲
    private String country;           // 国家
    
    public SearchCriteria() {}
    
    public SearchCriteria(String keyword, String fuzzyKeyword, Double similarity,
                         AgeGroup ageGroup, Integer minAge, Integer maxAge,
                         WeightClass weightClass, Double minWeight, Double maxWeight,
                         Continent continent, String country) {
        this.keyword = keyword;
        this.fuzzyKeyword = fuzzyKeyword;
        this.similarity = similarity;
        this.ageGroup = ageGroup;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.weightClass = weightClass;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.continent = continent;
        this.country = country;
    }
    
    // 构建器模式
    public static class Builder {
        private String keyword;
        private String fuzzyKeyword;
        private Double similarity;
        private AgeGroup ageGroup;
        private Integer minAge;
        private Integer maxAge;
        private WeightClass weightClass;
        private Double minWeight;
        private Double maxWeight;
        private Continent continent;
        private String country;
        
        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }
        
        public Builder fuzzyKeyword(String fuzzyKeyword) {
            this.fuzzyKeyword = fuzzyKeyword;
            return this;
        }
        
        public Builder similarity(Double similarity) {
            this.similarity = similarity;
            return this;
        }
        
        public Builder ageGroup(AgeGroup ageGroup) {
            this.ageGroup = ageGroup;
            return this;
        }
        
        public Builder minAge(Integer minAge) {
            this.minAge = minAge;
            return this;
        }
        
        public Builder maxAge(Integer maxAge) {
            this.maxAge = maxAge;
            return this;
        }
        
        public Builder ageRange(Integer minAge, Integer maxAge) {
            this.minAge = minAge;
            this.maxAge = maxAge;
            return this;
        }
        
        public Builder weightClass(WeightClass weightClass) {
            this.weightClass = weightClass;
            return this;
        }
        
        public Builder minWeight(Double minWeight) {
            this.minWeight = minWeight;
            return this;
        }
        
        public Builder maxWeight(Double maxWeight) {
            this.maxWeight = maxWeight;
            return this;
        }
        
        public Builder weightRange(Double minWeight, Double maxWeight) {
            this.minWeight = minWeight;
            this.maxWeight = maxWeight;
            return this;
        }
        
        public Builder continent(Continent continent) {
            this.continent = continent;
            return this;
        }
        
        public Builder country(String country) {
            this.country = country;
            return this;
        }
        
        public SearchCriteria build() {
            return new SearchCriteria(keyword, fuzzyKeyword, similarity, ageGroup, minAge, maxAge, 
                                    weightClass, minWeight, maxWeight, continent, country);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public String getFuzzyKeyword() {
        return fuzzyKeyword;
    }
    
    public void setFuzzyKeyword(String fuzzyKeyword) {
        this.fuzzyKeyword = fuzzyKeyword;
    }
    
    public Double getSimilarity() {
        return similarity;
    }
    
    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }
    
    public AgeGroup getAgeGroup() {
        return ageGroup;
    }
    
    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }
    
    public Integer getMinAge() {
        return minAge;
    }
    
    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }
    
    public Integer getMaxAge() {
        return maxAge;
    }
    
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }
    
    public WeightClass getWeightClass() {
        return weightClass;
    }
    
    public void setWeightClass(WeightClass weightClass) {
        this.weightClass = weightClass;
    }
    
    public Double getMinWeight() {
        return minWeight;
    }
    
    public void setMinWeight(Double minWeight) {
        this.minWeight = minWeight;
    }
    
    public Double getMaxWeight() {
        return maxWeight;
    }
    
    public void setMaxWeight(Double maxWeight) {
        this.maxWeight = maxWeight;
    }
    
    public Continent getContinent() {
        return continent;
    }
    
    public void setContinent(Continent continent) {
        this.continent = continent;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    /**
     * 检查是否有任何检索条件
     */
    public boolean hasAnyCriteria() {
        return keyword != null || hasFuzzyKeyword() || ageGroup != null || hasAgeRange() || 
               weightClass != null || hasWeightRange() || continent != null || country != null;
    }
    
    /**
     * 检查是否有关键词检索条件
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 检查是否有模糊关键词检索条件
     */
    public boolean hasFuzzyKeyword() {
        return fuzzyKeyword != null && !fuzzyKeyword.trim().isEmpty();
    }
    
    /**
     * 检查是否有年龄组别条件
     */
    public boolean hasAgeGroup() {
        return ageGroup != null;
    }
    
    /**
     * 检查是否有年龄范围条件
     */
    public boolean hasAgeRange() {
        return minAge != null || maxAge != null;
    }
    
    /**
     * 检查是否有体重级别条件
     */
    public boolean hasWeightClass() {
        return weightClass != null;
    }
    
    /**
     * 检查是否有体重范围条件
     */
    public boolean hasWeightRange() {
        return minWeight != null || maxWeight != null;
    }
    
    /**
     * 检查是否有大洲条件
     */
    public boolean hasContinent() {
        return continent != null;
    }
    
    /**
     * 检查是否有国家条件
     */
    public boolean hasCountry() {
        return country != null && !country.trim().isEmpty();
    }
    
    /**
     * 验证年龄范围参数
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    public String validateAgeRange() {
        if (minAge != null && maxAge != null && minAge > maxAge) {
            return "最小年龄不能大于最大年龄";
        }
        if (minAge != null && (minAge < 0 || minAge > 150)) {
            return "最小年龄必须在0-150之间";
        }
        if (maxAge != null && (maxAge < 0 || maxAge > 150)) {
            return "最大年龄必须在0-150之间";
        }
        return null;
    }
    
    /**
     * 验证体重范围参数
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    public String validateWeightRange() {
        if (minWeight != null && maxWeight != null && minWeight > maxWeight) {
            return "最小体重不能大于最大体重";
        }
        if (minWeight != null && (minWeight < 0 || minWeight > 500)) {
            return "最小体重必须在0-500公斤之间";
        }
        if (maxWeight != null && (maxWeight < 0 || maxWeight > 500)) {
            return "最大体重必须在0-500公斤之间";
        }
        return null;
    }
    
    /**
     * 验证相似度参数
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    public String validateSimilarity() {
        if (similarity != null && (similarity < 0.0 || similarity > 1.0)) {
            return "相似度必须在0.0-1.0之间";
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "SearchCriteria{" +
                "keyword='" + keyword + '\'' +
                ", fuzzyKeyword='" + fuzzyKeyword + '\'' +
                ", similarity=" + similarity +
                ", ageGroup=" + ageGroup +
                ", minAge=" + minAge +
                ", maxAge=" + maxAge +
                ", weightClass=" + weightClass +
                ", minWeight=" + minWeight +
                ", maxWeight=" + maxWeight +
                ", continent=" + continent +
                ", country='" + country + '\'' +
                '}';
    }
}
