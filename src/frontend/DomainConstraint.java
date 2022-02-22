package frontend;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @overview Defines a domain constraint of some attribute.
 * @author dmle
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainConstraint {
  /** Define type constants used in the {@link type()} field, 
   * e.g. @DomainConstraint(type=Type.String,...)*/
  public static enum Type {
    /**string data type*/
    String,
    /**character data type*/
    Char,
    /**integer data type*/
    Integer,
    /**long data type*/
    Long,
    /**float data type*/
    Float,
    /**double data type*/
    Double,
    /**object data type*/
    Object,
    /** a domain specific type, e.g. Student, Module, Customer, etc. */
    UserDefined,
    /**data type is unknown*/
    Null, 
    /**represents a collection type, e.g. List, Vector, etc.*/
    Collection;
    
    
    /**
     * @effects return <tt>true</tt> if this type is a numeric type (i.e. one of Integer, Long, etc.), 
     * otherwise return <tt>false</tt>
     */
    public boolean isNumeric() {
      String n = this.name();
      return (n.equals(Type.Integer.name()) ||
          //n.equals(Type.PInteger.name()) ||
          n.equals(Type.Long.name()) ||
          //n.equals(Type.PLong.name()) ||
          n.equals(Type.Float.name()) ||
          //n.equals(Type.PFloat.name()) ||
          n.equals(Type.Double.name())
          //n.equals(Type.PDouble.name())          
      );
    }
  };
  
  /**the data type, which is one of the types defined in {@link Type} */
  public Type type() default Type.Null;
  /**whether or not the data value can changed*/
  public boolean mutable() default true;
  /**whether or not the data value is required*/
  public boolean optional() default true;
  /**the maximum length of data values*/
  public int length() default  -1;
  /**the lower bound of data values (applicable to numeric types only)*/
  public double min() default Double.NaN;
  /**the upper bound of data values (applicable to numeric types only)*/
  public double max() default Double.NaN;
}
