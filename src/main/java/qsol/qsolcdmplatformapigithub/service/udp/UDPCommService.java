package qsol.qsolcdmplatformapigithub.service.udp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import qsol.qsolcdmplatformapigithub.domain.UniqueNumber;
import qsol.qsolcdmplatformapigithub.exception.udp.UniqueNumberNotFoundException;
import qsol.qsolcdmplatformapigithub.repository.UniqueNumberRepository;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@RequiredArgsConstructor
@Slf4j
@Service
public class UDPCommService {

    private final UniqueNumberRepository uniqueNumberRepository;
    private static final int MY_SERVER_PORT = 20050;
    private static final int SERVER_PORT = 20099;
    private final byte[] buf = new byte[10]; // 통신 프로토콜에 의거, 응답에 대한 udp 데이터의 길이도 10byte 고정
    private DatagramSocket socket = null;
    private String unique;


// - - - - - - - - - - - - - - - - - - - - - - -  S T A R T - - - - - - - - - - - - - - - - - - - - - - -
    public void udpStartRequest(String ip) throws UnknownHostException { // Client에 데이터 수집 시작 요청
        log.info("--- udpStartRequest Run ---");
        InetAddress ia = InetAddress.getByName(ip); // 통신 ip를 지정
        try {
            socket = new DatagramSocket(MY_SERVER_PORT); // 소켓 오픈. 본인의 port를 지정

            ByteBuffer buffer = ByteBuffer.allocate(10); // 통신 프로토콜에 의거, 데이터를 buffer, packet에 담아 upd 전송. 길이 10
            clear(buffer); // 버퍼 클리어
            buffer.order(ByteOrder.BIG_ENDIAN);// BIG_ENDIAN 형식

            byte[] udpProtocol = getUdpProtocol();
            buffer.put(udpProtocol); // 설정 값을 buffer에 담기
            byte[] bufferData = buffer.array();

            DatagramPacket dp = new DatagramPacket( // packet으로 옮겨 담기
                    bufferData, bufferData.length, ia, SERVER_PORT);

            socket.send(dp); // 소켓으로 udp 전송
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            socket.close(); // 전송이 완료되면 Client로 부터 응답 받을 소켓을 새로 열어야하기에, 소켓 닫기
        }
    }


    public void udpStartResponse() { // Client로 부터 데이터 수집 시작 요청에 대한 응답 받기
        log.info("--- udpStartResponse Run ---");
        try {
            socket = new DatagramSocket(MY_SERVER_PORT); // Client로 부터 응답 받기 위한 소켓을 오픈
            DatagramPacket packet = new DatagramPacket(buf, buf.length); // 마찬가지로 packet 생성
            socket.receive(packet); // udp 수신 대기

            // ▼ ▼ ▼ Client로 부터 udp 수신 완료 ▼ ▼ ▼

            String hexText = new BigInteger(packet.getData()).toString(16); // packet 데이터를 16진수로 된 String으로 변환
            System.out.println("hexText : \n" + hexText);
            String uniqueNumber = hexText.substring(10, 18); // 응답 받은 데이터 중에서 이 후 작업에 필요하게 될 [측정 고유 번호] 부분만 추출하여 DB에 저장.

            uniqueNumberRepository.save( // DB 저장 (jpa)
                    UniqueNumber.builder()
                            .value(uniqueNumber)
                            .build()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            socket.close(); // 작업 완료 후 소켓 닫기
        }
    }


// - - - - - - - - - - - - - - - - - - - - - - - - S T O P - - - - - - - - - - - - - - - - - - - - - - - -
    public void udpStopResponse() { // Client로 부터 측정 종료 수신
        log.info("--- udpStartResponse Run ---");
        try {
            socket = new DatagramSocket(MY_SERVER_PORT); // Client로 부터 응답 받기 위한 소켓을 오픈

            DatagramPacket packet = new DatagramPacket(buf, buf.length); // 마찬가지로 packet 생성
            socket.receive(packet); // udp 수신 대기

            // ▼ ▼ ▼ Client로 부터 udp 수신 완료 ▼ ▼ ▼
            UniqueNumber uniqueNumber = uniqueNumberRepository.findTopByOrderByIdDesc()
                    .orElseThrow(UniqueNumberNotFoundException::new);
            // DB에 가장 최근에 저장한 [측정 고유 번호]를 가져옴
            unique = uniqueNumber.getValue();
            System.out.println("uniqueNumber : " + unique);

            String hexText = new BigInteger(packet.getData()).toString(16); // packet 데이터를 16진수로 된 String으로 변환
//            System.out.println("hexText : \n" + hexText);
            String ims = hexText.substring(0, 6);
            String command = hexText.substring(6, 8);
            String ack = hexText.substring(8, 10);
            String uniNumber = hexText.substring(10, 18);
            String checkSum = hexText.substring(18, 20);
            System.out.println("ims : " + ims
                    + "\ncommand : " + command
                    + "\nack : " + ack
                    + "\nuniqueNumber : " + uniNumber
                    + "\ncheckSum : " + checkSum);
            if (ims.equals("494d53") // 통신 프로토콜에 의거, 각각의 데이터 검사
                    && command.equals("05")
                    && ack.equals("01")
                    && uniNumber.equals(unique)) {
                log.info("--- Protocol Check Success ---");
            } else {
                // 데이터가 일치하지 않을 시의 Exception 추가 필요
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("--- stopResponse socket.close() ---");
            socket.close(); // 작업 완료 후 소켓 닫기
        }
    }

    public void udpStopRequest(String ip) throws UnknownHostException { // Client에 측정 종료 응답 송신
        log.info("--- udpStopRequest Run ---");
        InetAddress ia = InetAddress.getByName(ip); // 통신 IP를 지정
        try {
            socket = new DatagramSocket(MY_SERVER_PORT); // 소켓 오픈. 본인의 port를 지정
            ByteBuffer buffer = ByteBuffer.allocate(10); // 통신 프로토콜에 의거, 데이터를 buffer, packet에 담아 upd 전송. 길이 10
            clear(buffer); // 버퍼 클리어
            buffer.order(ByteOrder.BIG_ENDIAN);// BIG_ENDIAN 형식

            byte[] parseByte = new byte[8]; // DB에서 가져온 String 값의 [측정 고유 번호]를 1byte씩 나누어 넣기 위한 변수를 생성
//            String[] result = new String[8];
            int count;
            int substring = 0;
            for (count = 0; count < 4; count++) { // [측정 고유 번호] 자료형 변환
                parseByte[count] = Byte.parseByte(unique.substring(substring, (substring + 2)));
//                String hex = String.format("0x%02x", parseByte[count]);
//                result[count] = hex; // hex로 변환할 필요가 없었음
                substring += 2;
            }

            byte[] filler = new byte[]{
                    0x49, 0x4d, 0x53, //IMS(3)
                    0x05, // Command(1)
                    0x00, // ACK(1)
                    parseByte[0], parseByte[1], parseByte[2], parseByte[3] // 측정고유번호(4) // 해당 데이터를 제외한 나머지는 고정 값
                    // 0xFF // checksum(1), 자동 설정
            };
            buffer.put(filler); // 설정된 값을 buffer에 담기
            byte[] bufferData = buffer.array();

            DatagramPacket dp = new DatagramPacket( // packet으로 옮겨 담기
                    bufferData, bufferData.length, ia, SERVER_PORT);

            socket.send(dp); // 소켓으로 udp 전송
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("--- stopRequest socket.close() ---");
            socket.close(); // 전송이 완료되면 Client로 부터 응답 받을 소켓을 새로 열어야하기에, 소켓 닫기
        }
    }



    //////////////////////////////////////////


    public static void clear(Buffer buffer) {
        buffer.clear();
    }

    private byte[] getUdpProtocol() {
        return new byte[]{ // 데이터 수집 시작 요청은 보내는 데이터가 항상 고정 값
                0x49, 0x4d, 0x53, //IMS(3byte)
                0x03, // Command(1byte)
                0x00, // ACK(1byte)
                0x00, 0x00, 0x00, 0x00 // 측정고유번호(4byte)
                // 0xFF // checksum(1byte), 자동 설정

        };
    }
}

