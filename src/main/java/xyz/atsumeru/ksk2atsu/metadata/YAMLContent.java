package xyz.atsumeru.ksk2atsu.metadata;

import lombok.Data;

import java.util.List;

/**
 * Model for YAML metadata that appears in almost all archives in raw dump
 */
@Data
public class YAMLContent {
    public String URL;
    public String Title;

    public List<String> Artist;
    public List<String> Parody;
    public String Circle;
    public List<String> Publisher;
    public String Event;
    public List<String> Magazine;
    public List<String> Tags;
    public String Description;

    // Unused
    public int Pages;
    public int Released;
    public int Thumbnail;
    public List<String> Collections;
}
