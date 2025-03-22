package javalab.utility;

/**Class representing different resource handling enums or similar.*/
public class Resource {
    /**Enum of load mode for resources.
     * Default stands for loading from cache if possible & writing to it if not.
     * Direct stands for loading from db and releasing from cache
     * (because direct load is usually needed for data modification thus cache is outdated)*/
    public enum LoadMode {
        DEFAULT,
        DIRECT
    }
}