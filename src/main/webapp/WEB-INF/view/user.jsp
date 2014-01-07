<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
	<title>User Profile</title>
	<style>
	<!--
	body {
	    background: #5B211A repeat-x center top;
	    color: #000;
	    font-family: "Hoefler Text", Georgia, "Times New Roman", Times, serif;
	    margin: 0;
	    padding: 0;
	    text-align: center;
	    font-size: medium !important;
	    line-height: normal !important;
	    text-align: center;
	}
	#border {
	    width: 1000px;
	    margin: 0 auto 0 auto;
	    background: #F2E2C1 repeat-y center top;
	    padding: 0 15px;
	    border: 10px solid #411213;
    }
	/* Tan IE5 box model fix  Hides from IE5-mac */
	* html #rap {
	    width: 730px;
	    w\idth: 800px;
	}       
	-->     
	</style>
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
</head>

<body>
<div id="border"><br/>
<table style="width: 100%; border-top: 2px solid #000000; border-bottom: 2px solid #000000;"><tr>
        <td width="25%" align="center"><a href="/user">User</a></td>
        <td width="25%" align="center"><a href="/diet">Diet</a></td>
        <td width="25%" align="center"><a href="/exercise">Exercise</a></td>
        <td width="25%" align="center"><a href="/weight">Weight</a></td>
</tr></table><br/>

<form action="/user" method="get">
<select name='userId'>
<option value="">NEW USER</option>
<c:forEach items="${users}" var="userBuffer">
<option value="${userBuffer.id}" <c:if test="${userBuffer.id == user.id}">selected="selected"</c:if>>${userBuffer.firstName} ${userBuffer.lastName}</option>
</c:forEach>
</select>
<input type='submit' value='Select User'>
</form>
<br/><div style="font-weight: bold; text-decoration: underline;">User Information</div><br/>

<form:form commandName="user" action="/user/save" method="post">
<form:hidden path="id"/>
<table style="margin-left: auto; margin-right: auto;">
<%-- 	<tr><td>User ID:</td><td><c:if test="${not empty user && user.id != 0}">${user.id}</c:if></td></tr> --%>
	<tr><td>Username:</td><td><form:input path="username" maxlength="50" size="50"/></td></tr>
<%-- 	<tr><td>Password:</td><td><form:input path="password" maxlength="50" size="50"/></td></tr> --%>
	<tr><td>First Name:</td><td><form:input path="firstName" maxlength="50" size="50"/></td></tr>
	<tr><td>Last Name:</td><td><form:input path="lastName" maxlength="50" size="50"/></td></tr>
	<tr>
	    <td valign="top">Gender:</td>
	    <td>
	    	<form:radiobutton path="gender" value="MALE"/> male<br/>
	    	<form:radiobutton path="gender" value="FEMALE"/> female
	    </td>
	</tr>
	<tr><td>Age:</td><td><form:input path="age" maxlength="3" size="3"/></td></tr>
	<tr><td>Height in Inches:</td><td><form:input path="heightInInches" maxlength="5" size="5"/></td></tr>
	<tr>
	    <td>Activity Level:</td>
	    <td>
	    	<form:select path="activityLevel">
	    		<form:option value="SEDENTARY">Sedentary</form:option>
	    		<form:option value="LIGHTLY_ACTIVE">Lightly Active</form:option>
	    		<form:option value="MODERATELY_ACTIVE">Moderately Active</form:option>
	    		<form:option value="VERY_ACTIVE">Very Active</form:option>
	    		<form:option value="EXTREMELY_ACTIVE">Extremely Active</form:option>
	    	</form:select>
	    </td>
	</tr>
	<tr><td>Current weight:</td><td><c:if test="${not empty user && user.id != null}">${user.currentWeight}</c:if></td></tr>
	<tr><td>BMI:</td><td><c:if test="${not empty user && user.id != null}"><fmt:formatNumber type="number" maxFractionDigits="2" value="${user.bmi}" /></c:if></td></tr>
	<tr><td>Calories needed daily to maintain weight:</td><td><c:if test="${not empty user && user.id != null}">${user.maintenanceCalories}</c:if></td></tr>
	<tr><td>Daily Points:</td><td><c:if test="${not empty user && user.id != null}">${user.dailyPoints}</c:if></td></tr>
</table>
<br/><input type='submit' value='Create / Update User'><br/><br/>
</form:form>
<form>
		<c:if test="${not empty user && user.id != null}">
		<script>
			$(function() {

				function deleteUser() {
					var confirmed = confirm("Are you SURE you want to delete this user?");
					if(confirmed) {
						document.location.href = "/user/delete/${user.id}";
					}
				}
	
				$("#deleteButton").click(function() {
					deleteUser();
				});
	
			});
		</script>
        <input id="deleteButton" type="button" value="Delete User">
        </c:if>
</form>

</div>
</body>
</html>
