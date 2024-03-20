


public class Converters {

  public static <S, D> ObjectsConverter<S, D> newListConverter(ObjectConverter<S, D> objectConverter) {
        return new ListObjectConverter(objectConverter);
    }
  
}
