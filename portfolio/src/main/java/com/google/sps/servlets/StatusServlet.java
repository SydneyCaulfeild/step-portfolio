package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/status")
public class StatusServlet extends HttpServlet {

  String status;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();

    //if the uesr is already logged in
    if (userService.isUserLoggedIn()) {
      status = "logged in";
    } else {
      status= "logged out";
    }

    // Convert the server stats to JSON
    String json = convertToJsonUsingGson(status);
    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /**
   * Converts an arraylist into a JSON string using the Gson library. 
   */
  private String convertToJsonUsingGson(String status) {
    Gson gson = new Gson();
    String json = gson.toJson(status);
    return json;
  }
}
