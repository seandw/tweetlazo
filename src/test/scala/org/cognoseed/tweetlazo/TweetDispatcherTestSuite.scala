package org.cognoseed.tweetlazo

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import org.cognoseed.tweetlazo.TweetDispatcher.WatchHashtag
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuiteLike, BeforeAndAfterAll}
import twitter4j.{HashtagEntity, Status}

class TweetDispatcherTestSuite extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll
  with MockFactory {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("TweetDispatcher spawns a child when sent a WatchHashtag message") {
    val probe = TestProbe()
    val fakeMaker = (_: ActorRefFactory, _: String) => probe.ref
    val dispatcher = TestActorRef[TweetDispatcher](TweetDispatcher.props(fakeMaker))
    dispatcher ! WatchHashtag("test")

    awaitAssert(dispatcher.underlyingActor.children("test") == probe.ref)
  }

  test("TweetDispatcher relays tweets to a child when it finds watched hashtags") {
    val probe = TestProbe()
    val fakeMaker = (_: ActorRefFactory, _: String) => probe.ref
    val dispatcher = system.actorOf(TweetDispatcher.props(fakeMaker))
    dispatcher ! WatchHashtag("test")

    val fakeTweet = mock[Status]
    val fakeHashtag = mock[HashtagEntity]
    (fakeHashtag.getText _).expects().returning("test")
    (fakeTweet.getHashtagEntities _).expects().returning(Array(fakeHashtag))

    dispatcher ! fakeTweet

    probe.expectMsg(fakeTweet)
  }

}
