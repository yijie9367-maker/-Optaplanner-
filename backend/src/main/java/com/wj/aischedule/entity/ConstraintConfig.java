package com.wj.aischedule.entity;

import javax.persistence.*;

@Entity
@Table(name = "constraint_config")
public class ConstraintConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "constraint_key", nullable = false, unique = true, length = 60)
    private String constraintKey;

    @Column(name = "constraint_name", nullable = false, length = 60)
    private String constraintName;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "category", length = 20)
    private String category;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConstraintKey() { return constraintKey; }
    public void setConstraintKey(String constraintKey) { this.constraintKey = constraintKey; }
    public String getConstraintName() { return constraintName; }
    public void setConstraintName(String constraintName) { this.constraintName = constraintName; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
