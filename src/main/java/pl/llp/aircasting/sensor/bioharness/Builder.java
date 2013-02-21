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

  public NeedsCount packed10byteInts(int index)
  {
    return new Packed10Bytes(index);
  }

  public NeedsNumber<Integer> intFromBytes()
  {
    return new IntFromBytes<Integer>();
  }

  public NeedsSecond<Short> shortFromBytes()
  {
    return new IntFromBytes<Short>();
  }

  class IntFromBytes<T> implements NeedsNumber<T>, NeedsThird<T>, NeedsSecond<T>, NeedsFirst<T>, Complete<T>
  {
    int[] indexes = new int[4];
    boolean[] indexesSet = new boolean[4];

    @Override
    public NeedsThird<T> fourth(int index)
    {
      indexes[3] = index;
      indexesSet[3] = true;
      return this;
    }

    @Override
    public NeedsSecond<T> third(int index)
    {
      indexes[2] = index;
      indexesSet[2] = true;
      return this;
    }

    @Override
    public NeedsFirst<T> second(int index)
    {
      indexes[1] = index;
      indexesSet[1] = true;
      return this;
    }

    @Override
    public Complete<T> first(int index)
    {
      indexes[0] = index;
      indexesSet[0] = true;
      return this;
    }

    @Override
    public T value()
    {
      int temp = 0;

      for (int i = indexesSet.length - 1; i > 0; i--)
      {
        boolean set = indexesSet[i];
        if(set)
        {
          int index = indexes[i];
          temp |= (input[offset + index] & 0xFF);
        }
       temp = temp << 8;
      }
      int index = indexes[0];
      temp |= (input[offset + index] & 0xFF);

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
}

interface NeedsNumber<T>
{
  NeedsThird<T> fourth(int index);
  NeedsSecond<T> third(int index);
  NeedsFirst<T> second(int index);
  Complete<T> first(int index);
}

interface NeedsThird<T>
{
  public NeedsSecond<T> third(int index);
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