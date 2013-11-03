<!-- <& /masoncomponents/masoncommon &> -->
<!-- <& /masoncomponents/fittrackercommon &> -->
<%-- <%perl> --%>
// 	use FitTracker::DataAccess;
// 	use FitTracker::User;
// 	use FitTracker::Food;
// 	use FitTracker::FoodEaten;
// 	my $DATAACCESS = 'FitTracker::DataAccess';
// 	my $USER = 'FitTracker::User';
// 	my $FOOD = 'FitTracker::Food';
// 	my $FOODEATEN = 'FitTracker::FoodEaten';

// 	# FETCH USER, OR REDIRECT TO USER PAGE IF NO USER CURRENTLY SELECTED
// 	my $user;
// 	my $userCookie    = &getCookie( 'FITTRACKER_USER' );
// 	if( defined( $userCookie ) ) {
// 		$user = $USER->getById( $userCookie );
// 	} else {
// 		$m->redirect("fittracker.cgi?page=user");
// 	}
	
// 	my $monthParameter = &getParam( 'Month' );
// 	my $dayParameter = &getParam( 'Day' );
// 	my $yearParameter = &getParam( 'Year' );
// 	my $editFoodEatenParameter = &getParam( 'EditFoodEaten' );
// 	my $editFoodEatenServingParameter = &getParam( 'FoodEatenServing' );
// 	my $editFoodEatenQtyParameter = &getParam( 'FoodEatenQty' );
// 	my $actionParameter = &getParam( 'Action' );
	
// 	# INITIALIZE VARIABLES USED IN FORM FIELDS BELOW
// 	my $month;
// 	my $day;
// 	my $year;
// 	my $date;
// 	if( !defined( $monthParameter ) ) {
// 		$month = ('01','02','03','04','05','06','07','08','09','10','11','12')[(localtime)[4]];
// 	} else {
// 		$month = $monthParameter;
// 	}
// 	if( !defined( $dayParameter ) ) {
// 		$day = (localtime)[3];
// 		if( $day < 10 ) {
// 			$day = "0$day";
// 		}
// 	} else {
// 		$day = $dayParameter;
// 	}
// 	if( !defined( $yearParameter ) ) {
// 		$year = (localtime)[5] + 1900;
// 	} else {
// 		$year = $yearParameter;
// 	}
// 	$date = "$year-$month-$day";

// 	my $yesterday;
// 	my $tomorrow;
// 	my $database = $DATAACCESS->getDatabaseConnection();
// 	my $sql = "SELECT date('$date','-1 day');";
// 	my $resultSet = $database->selectall_arrayref($sql);
// 	foreach (@$resultSet) {
// 		$yesterday = $_->[0];
// 	}
// 	$sql = "SELECT date('$date','+1 day');";
// 	$resultSet = $database->selectall_arrayref($sql);
// 	foreach (@$resultSet) {
// 		$tomorrow = $_->[0];
// 	}

// 	# IF AN 'EDIT' OR 'DELETE' BUTTON WAS CLICKED, UPDATE THAT FOOD EATEN RECORD
// 	if( defined( $actionParameter ) 
// 			&& defined( $monthParameter ) 
// 			&& defined( $dayParameter ) 
// 			&& defined( $yearParameter ) 
// 			&& defined( $editFoodEatenParameter ) 
// 			&& defined( $editFoodEatenServingParameter ) 
// 			&& defined( $editFoodEatenQtyParameter ) ) {
// 		if( $actionParameter eq "Edit" ) {
// 			$user->addFoodEaten( 
// 					$FOOD->getById( $editFoodEatenParameter ), 
// 					$date,
// 					$editFoodEatenServingParameter,
// 					$editFoodEatenQtyParameter );
// 		} elsif( $actionParameter eq "Delete" ) {
// 			my $foodEaten = $FOODEATEN->getByPrimaryKey( $user->getId,
// 					$FOOD->getById( $editFoodEatenParameter ),
// 					$date);
// 			$foodEaten->delete();
// 		}
// 	}
	
// 	# GET ALL FOODS EATEN BY THIS USER ON THIS DAY
// 	my @foodsEaten = $user->getFoodsEaten( $date );
	
// </%perl>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
	<title>Diet</title>
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

	
	<table><tr><td align="center" valign="middle">
		<b>Recently Eaten Foods:</b><br/>
		<select name="FoodID">
		<c:forEach items="${foodsRecentlyEaten}" var="recentFood">
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
<%-- <%perl> --%>
// 	for( my $index = 0; $index < @foodsEaten; $index++ ) {
// 		my $foodEaten = $foodsEaten[$index];
// 		my @availableServingTypes = $foodEaten->getFood()->getAvailableServingTypes();
// </%perl>
// 	<tr>
// 	<form>
// 	<input type="hidden" name="page" value="diet">
<%-- 	<input type="hidden" name="EditFoodEaten" value="<% $foodEaten->getFood()->getId() %>"> --%>
<%-- 	<input type="hidden" name="Month" value="<% $month %>"> --%>
<%-- 	<input type="hidden" name="Day" value="<% $day %>"> --%>
<%-- 	<input type="hidden" name="Year" value="<% $year %>"> --%>
<%-- 		<td style="border-bottom: 1px solid black;" width="30%"><% $foodEaten->getFood()->getName() %></td> --%>
<!-- 		<td style="border-bottom: 1px solid black; text-align: center;"> -->
<%-- 			<input type="text" size="2" name="FoodEatenQty" value="<% $foodEaten->getServingQty() %>"> --%>
<!-- 		</td> -->
<!-- 		<td style="border-bottom: 1px solid black; text-align: center;"> -->
<!-- 			<select name="FoodEatenServing"> -->
<%-- <%perl> --%>
// 	foreach my $type ( @availableServingTypes ) {
// 		print "<option value='$type'";
// 		if( $foodEaten->getServingType() eq $type ) { print " selected"; }
// 		print ">$type</option>\n";
// 	}
// </%perl>
// 			</select>
// 		</td>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getCalories() ) %></td> --%>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getFat() ) %></td> --%>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getSaturatedFat() ) %></td> --%>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getSodium() ) %></td> --%>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getCarbs() ) %></td> --%>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getFiber() ) %></td> --%>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getSugar() ) %></td> --%>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getProtein() ) %></td> --%>
<%-- 		<td style="border-bottom: 1px solid black; text-align: center;"><% sprintf( "%d", $foodEaten->getPoints() ) %></td> --%>
<!-- 		<td style="border-bottom: 1px solid black; text-align: center;"><input type="submit" name="Action" value="Edit"></td> -->
<!-- 		<td style="border-bottom: 1px solid black; text-align: center;"> -->
<!-- 			<input type="submit" name="Action" value="Delete" onclick="javascript:return confirm('Are you SURE you want to delete this food entry?');"> -->
<!-- 		</td> -->
<%-- 	</form> --%>
<!-- 	</tr> -->
<%-- <%perl> --%>
// 	}
// 	my $caloriesForDay = $user->getCaloriesForDay( $date );
// 	my $proteinForDay = $user->getProteinForDay( $date );
// 	my $fatForDay = $user->getFatForDay( $date );
// 	my $saturatedFatForDay = $user->getSaturatedFatForDay( $date );
// 	my $carbsForDay = $user->getCarbsForDay( $date );
// 	my $fiberForDay = $user->getFiberForDay( $date );
// 	my $sugarForDay = $user->getSugarForDay( $date );
// 	my $sodiumForDay = $user->getSodiumForDay( $date );
// 	my $pointsForDay = $user->getPointsForDay( $date );
// </%perl>	
// 	<tr>
// 		<td style="border-bottom: 2px solid black;" width="30%"><br/>TOTAL: </td>
// 		<td style="border-bottom: 2px solid black; text-align: center;"><br/>&nbsp;</td>
// 		<td style="border-bottom: 2px solid black; text-align: center;"><br/>&nbsp;</td>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $caloriesForDay ) %></td> --%>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $fatForDay ) %></td> --%>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $saturatedFatForDay ) %></td> --%>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $sodiumForDay ) %></td> --%>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $carbsForDay ) %></td> --%>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $fiberForDay ) %></td> --%>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $sugarForDay ) %></td> --%>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $proteinForDay ) %></td> --%>
<%-- 		<td style="border-bottom: 2px solid black; text-align: center;"><br/><% sprintf( "%d", $pointsForDay ) %></td> --%>
<!-- 		<td style="border-bottom: 2px solid black; text-align: center;" colspan="2"> -->
<%-- 			<% sprintf( "%d", $caloriesForDay - $user->getExerciseCaloriesForDay( $date ) ) %> net cal.<br/> --%>
<%-- 			<% sprintf( "%d", $pointsForDay - $user->getExercisePointsForDay( $date ) ) %> net points --%>
<!-- 		</td> -->
<!-- 	</tr> -->
	</table>
	<br/>
	<form><input type="hidden" name="page" value="foods"><input type="submit" value="Add / Manage Foods"></form>
	<br/>
	
	
	
	
</div>
</body>
</html>
