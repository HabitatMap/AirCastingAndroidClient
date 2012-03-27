package pl.llp.aircasting.repository;

/**
* Created by ags on 26/03/12 at 17:37
*/
public interface ProgressListener
{

    public void onSizeCalculated(int workSize);

    public void onProgress(int progress);
}
