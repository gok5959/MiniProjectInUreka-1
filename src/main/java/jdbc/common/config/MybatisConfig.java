package jdbc.common.config;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.IOException;
import java.io.Reader;

public class MybatisConfig {
    static public SqlSession getSqlSession() throws IOException {
        Reader reader = Resources.getResourceAsReader("config/mybatis-config.xml");
        // Mybatis 기본 모듈 SqlSession 객체 생성 <= 팩토리 디자인 패턴
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession session = sqlSessionFactory.openSession(); // DB Access 작업 가능

        return session;
    }
}
