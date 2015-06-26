package org.cognoseed.tweetlazo

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.cognoseed.tweetlazo.GoalCounter._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}
import twitter4j.Status

class GoalCounterTestSuite extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll
  with ImplicitSender with MockFactory {

  override def afterAll(): Unit = {
    shutdown(system)
  }

  trait CommonGoalCounterTest {
    val counter = TestActorRef[GoalCounter](GoalCounter.props("test"))
    def fakeTweet(text: String, retweet: Boolean = false, repeat: Int = 1) = {
      val tweet = mock[Status]
      (tweet.getText _).expects().returning(text).repeat(repeat)
      (tweet.isRetweet _).expects().returning(retweet).repeat(repeat)
      tweet
    }
  }

  test("GoalCounter counts getting tweets") {
    new CommonGoalCounterTest {
      val tweet = fakeTweet("this is a tweet I guess", repeat = 2)
      counter ! tweet
      counter ! tweet
      assert(counter.underlyingActor.tweets === 2)
    }
  }

  test("GoalCounter counts found goals, goal tweets, and extra letters") {
    new CommonGoalCounterTest {
      val tweet = fakeTweet("GOooOOooL GOAL gooooolllllaaaaaazzzzzzzoooooo")
      counter ! tweet
      assert(counter.underlyingActor.goalTweets === 1 && counter.underlyingActor.goals === 3
        && counter.underlyingActor.extraLetters === 30)
    }
  }

  test("GoalCounter can send counts to whoever sent the request") {
    new CommonGoalCounterTest {
      counter ! Counts("test")
      expectMsg(CountsReply("test", 0, 0, 0, 0, 0))
    }
  }

  test("GoalCounter can count and spit out the results") {
    new CommonGoalCounterTest {
      val tweet1 = fakeTweet("RT idk, but there aren't going to be any goals", retweet = true)
      val tweet2 = fakeTweet("Okay, the goal of this tweet is to have a large gooooooooooooool")

      counter ! tweet1
      counter ! tweet2

      counter ! Counts("test")
      expectMsg(CountsReply("test", 2, 1, 1, 2, 13))
    }
  }

  test("GoalCounter can count and calculate the difference from what it spat out before") {
    new CommonGoalCounterTest {
      val tweet1 = fakeTweet("RT idk, but there aren't going to be any goals", retweet = true)
      val tweet2 = fakeTweet("Okay, the goal of this tweet is to have a large gooooooooooooool")

      counter ! tweet1
      counter ! CountsSinceLast("test")
      expectMsg(CountsSinceLastReply("test", 1, 1, 0, 0, 0))

      counter ! tweet2
      counter ! CountsSinceLast("test")
      expectMsg(CountsSinceLastReply("test", 1, 0, 1, 2, 13))
    }
  }

}
