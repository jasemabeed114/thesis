package utils

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession

object GraphUtils {

    internal fun findTop25Retweets(spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
            SELECT  COUNT(retweeted_status_id) as total, retweeted_status_id
            FROM retweets
            GROUP BY retweeted_status_id
            ORDER BY total DESC
            LIMIT 25
            """
        )
    }

    internal fun removeRetweetsLessThanThreshold(threshold: Int, spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
                SELECT  COUNT(retweeted_status_id) as total, retweeted_status_id
                FROM retweets
                GROUP BY retweeted_status_id
                HAVING total > $threshold
                """)
    }

    internal fun retrieveTweetsAboveThreshold(lowerBound: Int, upperBound: Int, data: Dataset<Row>, spark: SparkSession): Dataset<Row> {
        data.filter("count > $lowerBound and count < $upperBound")
            .createOrReplaceTempView("tweetsWithRetweets")

        return spark.sql(
            """
            SELECT *
            FROM tweets
            WHERE id IN (SELECT id FROM tweetsWithRetweets)
            """.trimIndent())
    }

    internal fun retrieveTweetsWithRetweetCounts(spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
                SELECT id, COUNT(id) as count
                FROM tweets
                JOIN retweets ON tweets.id == retweets.retweeted_status_id
                GROUP BY id
                ORDER BY count DESC
                """.trimIndent())
    }

    internal fun findPostRetweets(retweetId: Long, spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
                SELECT *
                FROM retweets
                WHERE retweeted_status_id=$retweetId
                ORDER BY created_at
                """)
    }

    internal fun findPostReplies(replyId: Long, spark: SparkSession): Dataset<Row> {
        return spark.sql(
            """
                SELECT *
                FROM replies
                WHERE in_reply_to_status_id=$replyId
                ORDER BY created_at
                """)
    }

    internal fun showPostsCountByUser(spark: SparkSession) {
        spark.sql("""
            SELECT COUNT(user_screen_name) as total_count, user_screen_name
            FROM tweets
            GROUP BY user_screen_name
            ORDER BY total_count DESC
            """).show()
    }
}