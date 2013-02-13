package pl.llp.aircasting.sensor.bioharness;

public class Builder
{
  private int offset;
  private byte[] input;

  public Builder(byte[] array, int offset)
  {
    this.offset = offset;
    this.input = array;
  }

  public static Builder builder(byte[] array, int offset)
  {
    return new Builder(array, offset);
  }

  public int singleByteValue(int index)
  {
    return input[offset + index] & 0xFF;
  }

  public NeedsCount packed10byteInts(int index)
  {
    return new Packed10Bytes(index);
  }

  public NeedsHigher<Integer> intFromBytes()
  {
    return new HighLowInt<Integer>();
  }

  class Packed10Bytes implements NeedsCount, CompleteMultiple
  {
    private final int index;
    private int count;

    public Packed10Bytes(int index)
    {
      this.index = index;
    }

    @Override
    public CompleteMultiple samples(int count)
    {
      this.count = count;
      return this;
    }


    @Override
    public int[] ints()
    {
      int[] result = new int[count];

      int sampleNo = 0;
      int localIndex = offset + index;
      while(sampleNo < count)
      {
        int firstLs  = input[localIndex]      & 0xFF;
        int firstMs  = input[localIndex + 1]  & 0x03;
        int first    = ((firstMs << 8)  | firstLs);
        result[sampleNo] = first;
        sampleNo++;
        if(sampleNo == count) break;

        int secondLs = input[localIndex + 1]  & 0xFC;
        int secondMs = input[localIndex + 2]  & 0x0F;
        int second   = ((secondMs << 8) | secondLs) >> 2;
        result[sampleNo] = second;
        sampleNo++;
        if(sampleNo == count) break;

        int thirdLs  = input[localIndex + 2]  & 0xF0;
        int thirdMs  = input[localIndex + 3]  & 0x3F;
        int third    = ((thirdMs << 8)  | thirdLs)   >> 4;
        result[sampleNo] = third;
        sampleNo++;
        if(sampleNo == count) break;

        int fourthLs = input[localIndex + 3]  & 0xC0;
        int fourthMs = input[localIndex + 4]  & 0xFF;
        int fourth   = ((fourthMs << 8) | fourthLs) >> 6;
        result[sampleNo] = fourth;
        sampleNo++;
        if(sampleNo == count) break;

        localIndex += 4;
      }

      return result;
    }
  }

  class HighLowInt<T> implements NeedsHigher<T>, NeedsLower<T>, Complete<T>
  {
    private int higher;
    private int lower;

    @Override
    public NeedsLower<T> higher(int index)
    {
      this.higher = index;
      return this;
    }

    @Override
    public Complete<T> lower(int index)
    {
      this.lower = index;
      return this;
    }

    @Override
    public T value()
    {
      int high =  input[offset + this.higher] & 0xFF;
      int low =   input[offset + this.lower]  & 0xFF;
      return (T) Integer.valueOf((high << 8) | low);
    }
  }
}

interface NeedsHigher<T>
{
  public NeedsLower<T> higher(int index);
}

interface NeedsLower<T>
{
  public Complete<T> lower(int index);
}

interface Complete<T>
{
  public T value();
}

interface NeedsCount
{
  public CompleteMultiple samples(int count);
}

interface CompleteMultiple
{
  public int[] ints();
}