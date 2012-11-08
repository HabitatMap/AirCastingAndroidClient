package pl.llp.aircasting.api.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeleteSessionResponse
{
  @Expose Boolean success;
  @Expose @SerializedName("no_such_session") Boolean noSuchSession = Boolean.FALSE;

  public DeleteSessionResponse()
  {
  }

  public DeleteSessionResponse(Boolean success)
  {
    this.success = success;
  }

  public Boolean isSuccess()
  {
    return success;
  }

  public Boolean noSuchSession()
  {
    return noSuchSession;
  }
}
