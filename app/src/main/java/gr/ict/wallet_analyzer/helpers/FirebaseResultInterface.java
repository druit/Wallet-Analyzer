package gr.ict.wallet_analyzer.helpers;

import java.util.ArrayList;

import data_class.History;

public interface  FirebaseResultInterface<T> {

   public void onSuccess(T data);

   public void onFailed (Throwable error) ;

}
