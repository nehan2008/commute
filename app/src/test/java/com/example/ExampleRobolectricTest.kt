package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChennaiTransitRepository
import com.example.model.JourneyFilter
import com.example.model.UserExperienceMode
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Chennai Move", appName)
  }

  @Test
  fun `test Chennai transit routing engine for elderly and youth`() {
    val stations = ChennaiTransitRepository.STATIONS
    assertTrue("Stations list must not be empty", stations.isNotEmpty())

    val central = stations.first { it.id == "central" }
    val guindy = stations.first { it.id == "guindy" }

    // Elderly mode test
    val elderlyJourneys = ChennaiTransitRepository.planJourneys(
      origin = central,
      destination = guindy,
      userMode = UserExperienceMode.ELDERLY,
      filter = JourneyFilter.ALL
    )
    assertTrue("Should generate routes between Central and Guindy", elderlyJourneys.isNotEmpty())
    val topElderly = elderlyJourneys.first()
    assertEquals("Elderly mode top route should minimize transfers", 0, topElderly.totalTransfers)

    // Youth mode test
    val youthJourneys = ChennaiTransitRepository.planJourneys(
      origin = central,
      destination = guindy,
      userMode = UserExperienceMode.YOUTH,
      filter = JourneyFilter.CHEAPEST
    )
    assertTrue("Should generate routes for youth mode", youthJourneys.isNotEmpty())
    val cheapest = youthJourneys.first()
    assertTrue("Cheapest fare should be <= ₹15", cheapest.totalFareRupees <= 15)
  }

  @Test
  fun `test community disruption upvote and report`() {
    val initialDisruptions = ChennaiTransitRepository.disruptions.value
    assertTrue("Should have initial disruptions", initialDisruptions.isNotEmpty())

    val firstId = initialDisruptions.first().id
    val initialVotes = initialDisruptions.first().upvotes
    ChennaiTransitRepository.upvoteDisruption(firstId)

    val updatedVotes = ChennaiTransitRepository.disruptions.value.first { it.id == firstId }.upvotes
    assertEquals("Upvote should increment vote count by 1", initialVotes + 1, updatedVotes)
  }

  @Test
  fun `test ECR and OMR station search`() {
    val ecrResults = ChennaiTransitRepository.searchStationsAndStops("ECR")
    assertTrue("Search for ECR should return ECR stops", ecrResults.isNotEmpty())
    assertTrue("Results should include Thiruvanmiyur ECR or Kottivakkam", ecrResults.any { it.nameEn.contains("ECR") || it.locality.contains("ECR") })

    val omrResults = ChennaiTransitRepository.searchStationsAndStops("OMR")
    assertTrue("Search for OMR should return OMR stops", omrResults.isNotEmpty())
  }
}
