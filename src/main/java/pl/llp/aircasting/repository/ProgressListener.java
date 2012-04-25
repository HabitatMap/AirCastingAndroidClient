package pl.llp.aircasting.repository;

public interface ProgressListener
{
    public void onSizeCalculated(int workSize);

    public void onProgress(int progress);
}
