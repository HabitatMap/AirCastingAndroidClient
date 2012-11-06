package pl.llp.aircasting.api.data;

import com.google.gson.annotations.Expose;

public class DeleteSessionResponse
{
  @Expose Boolean success;

  public Boolean isSuccess()
  {
    return success;
  }
}
