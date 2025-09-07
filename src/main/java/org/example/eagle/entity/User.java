package org.example.eagle.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private String hash;

    @Column(columnDefinition = "jsonb")
    private String addressJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public Address getAddress() {
        if (addressJson == null) return null;
        try {
            return new ObjectMapper().readValue(addressJson, Address.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAddress(Address address) {
        try {
            this.addressJson = new ObjectMapper().writeValueAsString(address);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAddressJson() { return addressJson; }
    public void setAddressJson(String addressJson) { this.addressJson = addressJson; }

    @Embeddable
    public static class Address {
        private String line1;
        private String line2;
        private String line3;
        private String town;
        private String county;
        private String postcode;

        public Address() {}
        public Address(String line1, String line2, String line3, String town, String county, String postcode) {
            this.line1 = line1;
            this.line2 = line2;
            this.line3 = line3;
            this.town = town;
            this.county = county;
            this.postcode = postcode;
        }
        public String getLine1() { return line1; }
        public void setLine1(String line1) { this.line1 = line1; }
        public String getLine2() { return line2; }
        public void setLine2(String line2) { this.line2 = line2; }
        public String getLine3() { return line3; }
        public void setLine3(String line3) { this.line3 = line3; }
        public String getTown() { return town; }
        public void setTown(String town) { this.town = town; }
        public String getCounty() { return county; }
        public void setCounty(String county) { this.county = county; }
        public String getPostcode() { return postcode; }
        public void setPostcode(String postcode) { this.postcode = postcode; }
    }
}
