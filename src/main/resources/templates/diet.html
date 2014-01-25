<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
	<title>Diet</title>
	<link rel="stylesheet" type="text/css" href="//code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css">
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
	<script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
	<script>
  		$(function() {
            //$.datepicker.formatDate( format, date, settings )
    		$("#datepicker").datepicker();
  		});
  	</script>
</head>

<body>
<div id="border"><br/>
<table style="width: 100%; border-top: 2px solid #000000; border-bottom: 2px solid #000000;"><tr>
        <td width="25%" align="center"><a href="/user">User</a></td>
        <td width="25%" align="center"><a href="/diet">Diet</a></td>
        <td width="25%" align="center"><a href="/exercise">Exercise</a></td>
        <td width="25%" align="center"><a href="/weight">Weight</a></td>
</tr></table><br/>

<form>
	<br/><b>Foods Eaten On:</b><br/>
	<input type="text" id="datepicker" />
	<input type="submit" value="Change Date">

	<table width="100%"><tr><td align="center" valign="middle">
		<b>Recently Eaten Foods:</b><br/>
		<select name="FoodID">
		<c:forEach items="${foodsEatenRecently}" var="recentFood">
			<option value="${recentFood.id}">${recentFood.name}</option>
		</c:forEach>	
		</select>
		<input type="submit" name="Action" value="Add Food">
</form>
	</td><td align="center" valign="middle">
		<form method="get">
			&nbsp;<br/>
			<input type="hidden" name="page" value="searchfoods">
			<input type="text" name="FoodName">
			<input type="submit" value="Search Foods">
		</form>
	</td></tr></table>

	<table width="100%" cellspacing="0" cellpadding="2">
	<tr>
		<td style="border-bottom: 2px solid black;" width="30%">Food</td>
		<td style="border-bottom: 2px solid black; text-align: center;"># of Servings</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Serving Size</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Calories</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Fat</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Sat. Fat</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Sodium</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Carbs</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Fiber</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Sugar</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Protein</td>
		<td style="border-bottom: 2px solid black; text-align: center;">Points</td>
		<td style="border-bottom: 2px solid black; text-align: center;">&nbsp;</td>
		<td style="border-bottom: 2px solid black; text-align: center;">&nbsp;</td>
	</tr>
	<c:forEach items="${foodsEatenThisDate}" var="foodEaten">
	<!-- CREATE CONTROLLER LISTENING FOR UPDATE-FOOD-EATEN REQUESTS -->
	<tr>
	<form>
	    <td style="border-bottom: 1px solid black;" width="30%">${foodEaten.food.name}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;"><input type="text" size="2" name="FoodEatenQty" value="${foodEaten.servingQty}"></td>
	    <td style="border-bottom: 1px solid black; text-align: center;">
	        <select name="FoodEatenServing">
                <c:choose>
                    <c:when test="${foodEaten.servingType == 'CUSTOM'}">
                        <option value='CUSTOM' selected>CUSTOM</option>
                    </c:when>
                    <c:otherwise>
                        <option value='ounce' <c:if test="${foodEaten.servingType == 'ounce'}">selected</c:if>>ounce</option>
                        <option value='cup' <c:if test="${foodEaten.servingType == 'cup'}">selected</c:if>>cup</option>
                        <option value='pound' <c:if test="${foodEaten.servingType == 'pound'}">selected</c:if>>pound</option>
                        <option value='pint' <c:if test="${foodEaten.servingType == 'pint'}">selected</c:if>>pint</option>
                        <option value='tablespoon' <c:if test="${foodEaten.servingType == 'tablespoon'}">selected</c:if>>tablespoon</option>
                        <option value='teaspoon' <c:if test="${foodEaten.servingType == 'teaspoon'}">selected</c:if>>teaspoon</option>
                        <option value='gram' <c:if test="${foodEaten.servingType == 'gram'}">selected</c:if>>gram</option>
                    </c:otherwise>
                </c:choose>
	        </select>
	    </td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.calories}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.fat}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.saturatedFat}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.sodium}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.carbs}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.fiber}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.sugar}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.protein}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">${foodEaten.points}</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">Update</td>
	    <td style="border-bottom: 1px solid black; text-align: center;">Delete</td>
	</form>
	</tr>
	</c:forEach>

    <tr>
        <td style="border-bottom: 2px solid black;" width="30%"><br/>TOTAL: </td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>&nbsp;</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>&nbsp;</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${caloriesForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${fatForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${saturatedFatForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${sodiumForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${carbsForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${fiberForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${sugarForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${proteinForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;"><br/>${pointsForDay}</td>
        <td style="border-bottom: 2px solid black; text-align: center;" colspan="2">net cal.<br/>net points</td>
    </tr>
    </table>

	<br/>
	<form><input type="hidden" name="page" value="foods"><input type="submit" value="Add / Manage Foods"></form>
	<br/>

</div>
</body>
</html>
