package org.cognoseed.tweetlazo

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.cognoseed.tweetlazo.TweetDispatcher.WatchHashtag
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}
import twitter4j.{HashtagEntity, Status}

class TweetDispatcherTestSuite extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll
  with ImplicitSender with MockFactory {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  trait CommonTweetDispatcherTest {
    val maker = mockFunction[ActorRefFactory, String, ActorRef]
    maker.expects(*, "test").returning(testActor)
    val dispatcher = TestActorRef[TweetDispatcher](TweetDispatcher.props(maker))
    dispatcher ! WatchHashtag("test")
  }

  test("TweetDispatcher spawns a child when sent a WatchHashtag message") {
    new CommonTweetDispatcherTest {
      awaitAssert(dispatcher.underlyingActor.children("test") == testActor)
    }
  }

  test("TweetDispatcher relays tweets to a child when it finds watched hashtags") {
    new CommonTweetDispatcherTest {
      val fakeTweet = mock[Status]
      val fakeHashtag = mock[HashtagEntity]
      (fakeHashtag.getText _).expects().returning("test")
      (fakeTweet.getHashtagEntities _).expects().returning(Array(fakeHashtag))

      dispatcher ! fakeTweet
      expectMsg(fakeTweet)
    }
  }

}
