package de.guc;

import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.NoCache;
import org.jboss.resteasy.reactive.RestPath;


import de.guc.dto.MemberDto;
import de.guc.entities.EmailUpdateEntity;
import de.guc.entities.MemberEntity;
import de.guc.entities.ResourcesEntity;
import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("v1/member")
@Authenticated
public class MemberResource {

    @Inject
    JsonWebToken accessToken;
    @Inject ObjectMapper mapper;
    
    @GET
    @Path("{id}")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public MemberDto getMemberById(@RestPath("id") String memberId) {
        checkAuthorization(UUID.fromString(memberId), true);
        final var member = MemberEntity.byId(UUID.fromString(memberId));
        final var dto = MemberDto.fromEntity(member.orElseThrow(() -> new NotFoundException()));
        return dto;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Path("{id}")
    public MemberDto updateMember(@RestPath("id") String memberId, @Valid MemberDto member) {
        if (!UUID.fromString(memberId).equals(member.getId())) {
            Log.warnf("Attempt to store a member-object with id %s under id %s", member.getId(), memberId);
            throw new BadRequestException();
        }
        final var existingMember = MemberEntity.byId(UUID.fromString(memberId));
        if (existingMember.isEmpty()) {
            Log.warnf("Did not find member with id %s. Returning 404", memberId);
            throw new NotFoundException();
        }
        this.checkAuthorization(UUID.fromString(memberId), false);
        final var entityToUpdate = existingMember.get();
        this.checkEmailUpdate(entityToUpdate, member);
        this.updateMember(entityToUpdate, member);
        entityToUpdate.persistAndFlush();
        return MemberDto.fromEntity(entityToUpdate);
    }

    private void checkAuthorization(final UUID resourceId, final boolean  read) {
        final String subject = this.accessToken.getSubject();
        ResourcesEntity.hasAccess(UUID.fromString(subject), resourceId).orElseThrow(() -> new UnauthorizedException());
    }

    private void checkEmailUpdate(@Nonnull MemberEntity member, @Nonnull MemberDto dto) {
        final var updates = new ArrayList<EmailUpdateEntity>();
         
       if (!member.email.equals(dto.getEmail())) {
            // email differs
           if (member.emailList) {
               final var removeOldAddress = new EmailUpdateEntity();
               removeOldAddress.emailAddress = member.email;
               removeOldAddress.change = EmailUpdateEntity.ChangeType.REMOVE;
               updates.add(removeOldAddress);
           }
           if(dto.isEmailList()){
               final var insertNewAddress = new EmailUpdateEntity();
               insertNewAddress.emailAddress = dto.getEmail();
               insertNewAddress.change = EmailUpdateEntity.ChangeType.INSERT;
               updates.add(insertNewAddress);
           }
       } else if (member.emailList != dto.isEmailList()) {
           if(member.emailList) {
               final var removeOldAddress = new EmailUpdateEntity();
               removeOldAddress.emailAddress = member.email;
               removeOldAddress.change = EmailUpdateEntity.ChangeType.REMOVE;
               updates.add(removeOldAddress);
           } else {
               final var insertNewAddress = new EmailUpdateEntity();
               insertNewAddress.emailAddress = dto.getEmail();
               insertNewAddress.change = EmailUpdateEntity.ChangeType.INSERT;
               updates.add(insertNewAddress);
           } 
       }

       updates.stream().forEach(e -> e.persist());
    }

    private void updateMember(@Nonnull MemberEntity member, @Nonnull MemberDto dto) {
        // we don't update id, dfvNumber, and entryDate for a reason.
        member.city = dto.getCity();
        member.dayOfBirth = dto.getDayOfBirth();
        member.dse = dto.isDse();
        member.email = dto.getEmail();
        member.emailList = dto.isEmailList();
        member.exitDate = dto.getExitDate();
        member.gender = dto.getGender();
        member.givenName = dto.getGivenName();
        member.mobile = dto.getMobile();
        member.name = dto.getName();
        member.rabatt = dto.isDfvDiscount();
        member.street = dto.getStreet();
        if (!dto.getState().equals(member.state)) {
            // only update stateEffective, iff the state has changed.
            member.stateEffective = dto.getStateEffective();
        }
        member.state = dto.getState();
        member.zipCode = dto.getZipCode();
    }
}