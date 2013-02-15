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

  public NeedsSecond<Integer> intFromBytes()
  {
    return new HighLowInt<Integer>();
  }

  public NeedsNumber<Integer> signedShortFromBytes()
  {
    return new SignedNumber<Integer>();
  }

  class SignedNumber<T> implements NeedsNumber<T>, NeedsSecond<T>, NeedsFirst<T>, Complete<T>
  {
    private int second;
    private int first;

    @Override
    public NeedsFirst<T> second(int index)
    {
      this.second = index;
      return this;
    }

    @Override
    public Complete<T> first(int index)
    {
      this.first = index;
      return this;
    }

    @Override
    public T value()
    {
      int temp = (input[offset + second] & 0xFF);
      temp = temp << 8;
      temp |= (input[offset + first]   & 0xFF);

      short s = (short) temp;
      temp = s;
      return (T) Integer.valueOf(temp);
    }
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

  class HighLowInt<T> implements NeedsSecond<T>, NeedsFirst<T>, Complete<T>
  {
    private int second;
    private int first;

    @Override
    public NeedsFirst<T> second(int index)
    {
      this.second = index;
      return this;
    }

    @Override
    public Complete<T> first(int index)
    {
      this.first = index;
      return this;
    }

    @Override
    public T value()
    {
      int high =  input[offset + this.second] & 0xFF;
      int low =   input[offset + this.first]  & 0xFF;
      return (T) Integer.valueOf((high << 8) | low);
    }
  }
}

interface NeedsNumber<T>
{
  NeedsFirst<T> second(int index);
  Complete<T> first(int index);
}

interface NeedsSecond<T>
{
  public NeedsFirst<T> second(int index);
}

interface NeedsFirst<T>
{
  public Complete<T> first(int index);
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