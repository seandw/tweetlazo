package org.cognoseed.tweetlazo

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestProbe, TestActorRef, TestKit}
import org.cognoseed.tweetlazo.GoalCounter.{CountsReply, Counts}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}
import twitter4j.Status

class GoalCounterTestSuite extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll
  with ImplicitSender with MockFactory {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("GoalCounter counts getting tweets") {
    val counter = TestActorRef[GoalCounter](GoalCounter.props("test"))
    val fakeTweet = mock[Status]
    (fakeTweet.getText _).expects().returning("this is a tweet I guess").repeat(2)
    (fakeTweet.isRetweet _).expects().returning(false).repeat(2)
    counter ! fakeTweet
    counter ! fakeTweet
    assert(counter.underlyingActor.tweets === 2)
  }

  test("GoalCounter counts found goals, goal tweets, and extra letters") {
    val counter = TestActorRef[GoalCounter](GoalCounter.props("test"))
    val fakeTweet = mock[Status]
    (fakeTweet.getText _).expects().returning("GOooOOooL GOAL gooooolllllaaaaaazzzzzzzoooooo")
    (fakeTweet.isRetweet _).expects().returning(false)
    counter ! fakeTweet
    assert(counter.underlyingActor.goalTweets === 1 && counter.underlyingActor.goals === 3
      && counter.underlyingActor.extraLetters === 30)
  }

  test("GoalCounter can send counts to whoever sent the request") {
    val counter = TestActorRef[GoalCounter](GoalCounter.props("test"))
    counter ! Counts("test")
    expectMsg(CountsReply("test", 0, 0, 0, 0, 0))
  }

  test("putting it all together...") {
    val counter = TestActorRef[GoalCounter](GoalCounter.props("test"))

    val fakeTweets = mock[Status]
    (fakeTweets.getText _).expects().returning("RT idk, but there aren't going to be any goals")
    (fakeTweets.isRetweet _).expects().returning(true)
    (fakeTweets.getText _).expects().returning("Okay, the goal of this tweet is to have a large gooooooooooooool")
    (fakeTweets.isRetweet _).expects().returning(false)

    counter ! fakeTweets
    counter ! fakeTweets

    counter ! Counts("test")
    expectMsg(CountsReply("test", 2, 1, 1, 2, 13))
  }

}
